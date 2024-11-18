package com.example.gateway.bpp

import com.example.core.exception.InvalidObjectIdFormatException
import com.example.gateway.infrastructure.bpp.ValidObjectIdFormatBeanPostProcessor
import com.example.gateway.infrastructure.mapper.rest.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.infrastructure.rest.PassController
import com.example.gateway.infrastructure.rest.dto.PassDto
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.gateway.util.PassDtoFixture.passDtoWithInvalidIdFormats
import com.example.gateway.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.gateway.util.PassProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture.successfulCreatePassResponse
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassResponse
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.support.AopUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class ValidObjectIdFormatBeanPostProcessorTest {
    @MockK
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @InjectMockKs
    private lateinit var passController: PassController

    private val beanPostProcessor: ValidObjectIdFormatBeanPostProcessor = ValidObjectIdFormatBeanPostProcessor()

    @BeforeTest
    fun runBeforeInitialization() {
        beanPostProcessor.postProcessBeforeInitialization(passController, BEAN_NAME)
    }

    @Test
    fun `processing controller with necessary params must be proxied`() {
        // WHEN
        val beanAfterProcessing = beanPostProcessor.postProcessAfterInitialization(passController, BEAN_NAME)

        // THEN
        assertTrue(AopUtils.isCglibProxy(beanAfterProcessing), message = "Bean must be proxy")
    }

    @Test
    fun `processing bean, which not match BPP logic, must return bean`() {
        // WHEN
        val beanNotToBeProxied = beanPostProcessor.postProcessAfterInitialization(
            natsMessagePublisher,
            natsMessagePublisher::class.java.simpleName
        )

        // THEN
        assertThat(beanNotToBeProxied).isEqualTo(natsMessagePublisher)
        assertFalse(AopUtils.isCglibProxy(beanNotToBeProxied), message = "Bean must not be proxy")
    }

    @Test
    fun `controller method with dto, which has invalid field values, should throw custom runtime exception`() {
        // GIVEN
        val proxiedController = getProxiedController()
        val failureResponse = failureCreatePassResponseWithPassOwnerNotFound(passDtoWithInvalidIdFormats.passOwnerId)
        every { doCreateRequest(passDtoWithInvalidIdFormats) } returns failureResponse.toMono()

        // WHEN
        val created = Mono.defer { proxiedController.create(passDtoWithInvalidIdFormats) }

        // THEN
        created.test().verifyError<InvalidObjectIdFormatException>()
    }

    @Test
    fun `controller method with dto, which has valid field values, should pass`() {
        // GIVEN
        val proxiedController = getProxiedController()

        every { doCreateRequest(passDto) } returns successfulCreatePassResponse(protoPass).toMono()

        // WHEN
        val created = proxiedController.create(passDto)

        // THEN
        created.test()
            .assertNext { assertThat(it).isEqualTo(passDto) }
            .verifyComplete()
    }

    private fun getProxiedController(): PassController {
        return beanPostProcessor.postProcessAfterInitialization(passController, BEAN_NAME) as PassController
    }

    private fun doCreateRequest(passDto: PassDto): Mono<CreatePassResponse> {
        return natsMessagePublisher.request(
            CREATE,
            passDto.toCreatePassRequest(),
            CreatePassResponse.parser()
        )
    }

    companion object {
        private const val BEAN_NAME = "passController"
    }
}
