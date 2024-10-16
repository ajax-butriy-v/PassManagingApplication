package com.example.gateway.configuration

import com.example.gateway.exception.InvalidObjectIdFormatException
import com.example.gateway.proto.PassDtoFixture.passDto
import com.example.gateway.proto.PassDtoFixture.passDtoWithInvalidIdFormats
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.web.rest.PassController
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.util.PassFixture.passFromDb
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.successfulCreatePassResponse
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.support.AopUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class ValidObjectIdFormatBeanPostProcessorTest {
    private val natsClient: NatsClient = mockk()

    private val passController = PassController(natsClient)

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
            natsClient,
            natsClient::class.java.simpleName
        )

        // THEN
        assertThat(beanNotToBeProxied).isEqualTo(natsClient)
        assertFalse(AopUtils.isCglibProxy(beanNotToBeProxied), message = "Bean must not be proxy")
    }

    @Test
    fun `controller method with dto, which has invalid field values, should throw custom runtime exception`() {
        // GIVEN
        val proxiedController = getProxiedController()
        every {
            natsClient.request(
                CREATE,
                passDtoWithInvalidIdFormats.toCreatePassRequest(),
                CreatePassResponse.parser()
            )
        } returns failureCreatePassResponseWithPassOwnerNotFound(passDtoWithInvalidIdFormats.passOwnerId).toMono()

        // WHEN
        val created = Mono.defer { proxiedController.create(passDtoWithInvalidIdFormats) }

        // THEN
        created.test().verifyError<InvalidObjectIdFormatException>()
    }

    @Test
    fun `controller method with dto, which has valid field values, should pass`() {
        // GIVEN
        val proxiedController = getProxiedController()
        val passToReturn = passFromDb.copy(
            passOwnerId = ObjectId(passDto.passOwnerId),
            passTypeId = ObjectId(passDto.passTypeId)
        )

        every {
            natsClient.request(
                CREATE,
                passDto.toCreatePassRequest(),
                CreatePassResponse.parser()
            )
        } returns successfulCreatePassResponse(passToReturn).toMono()

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

    companion object {
        private const val BEAN_NAME = "passController"
    }
}
