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
                .transform { mono -> onParseError(mono, controller.responseClassType) }
                .subscribe { response ->
                    controller.connection.publish(it.replyTo, response.toByteArray())
                }
        }
        return dispatcher.subscribe(controller.subject, controller.queueGroup, messageHandler)
    }

    private fun <R : GeneratedMessageV3> onParseError(result: Mono<R>, responseClass: Class<R>): Mono<R> {
        return result.onErrorResume { throwable ->
            val message = throwable.message.orEmpty()
            val builder = responseClass.getMethod("newBuilder").invoke(null)
            val failureBuilder = builder.javaClass.getMethod("getFailureBuilder").invoke(builder)
            failureBuilder.javaClass.getMethod("setMessage", String::class.java).invoke(failureBuilder, message)
            val fallbackResponse = builder.javaClass.getMethod("build").invoke(builder) as? R
            fallbackResponse?.toMono() ?: Mono.error(throwable)
        }
    }
}
