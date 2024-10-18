package com.example.passmanagersvc.configuration

import com.example.passmanagersvc.nats.controller.NatsController
import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Dispatcher
import io.nats.client.MessageHandler
import io.nats.client.Subscription
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class NatsControllerBeanPostProcessor(private val dispatcher: Dispatcher) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean is NatsController<*, *>) {
            dispatch(bean)
        }
        return bean
    }

    private fun <T : GeneratedMessageV3, R : GeneratedMessageV3> dispatch(
        controller: NatsController<T, R>,
    ): Subscription {
        val messageHandler = MessageHandler {
            Mono.fromCallable { controller.parser }
                .map { parser -> parser.parseFrom(it.data) }
                .flatMap { parsedData -> controller.handle(parsedData) }
                .onErrorResume { throwable -> onParseError(throwable, controller.responseClass) }
                .subscribe { response ->
                    controller.connection.publish(it.replyTo, response.toByteArray())
                }
        }
        return dispatcher.subscribe(controller.subject, controller.queueGroup, messageHandler)
    }

    private fun <R : GeneratedMessageV3> onParseError(throwable: Throwable, responseClass: R): Mono<R> {
        val message = throwable.message.orEmpty()
        val responseBuilder = responseClass.toBuilder()

        val failureDescriptor = responseClass.descriptorForType.findFieldByName("failure")
        val messageDescriptor = failureDescriptor.messageType.findFieldByName("message")
        val response = responseBuilder.run {
            val failure = newBuilderForField(failureDescriptor).setField(messageDescriptor, message).build()
            setField(failureDescriptor, failure)
        }.build()

        return (response as? R)?.toMono() ?: Mono.error(throwable)
    }
}
