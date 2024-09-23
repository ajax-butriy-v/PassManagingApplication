package com.example.passmanager.configuration

import com.example.passmanager.exception.InvalidObjectIdFormatException
import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture
import com.example.passmanager.util.PassFixture.dtoWithValidIdFormats
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.web.controller.PassController
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.support.AopUtils
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class ValidObjectIdFormatBeanPostProcessorTest {

    private val passService: PassService = mockk()

    private val passManagementService: PassManagementService = mockk()

    private val passController = PassController(passService, passManagementService)

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
        val beanNotToBeProxied = beanPostProcessor.postProcessAfterInitialization(passService, "passService")

        // THEN
        assertThat(beanNotToBeProxied).isEqualTo(passService)
        assertFalse(AopUtils.isCglibProxy(beanNotToBeProxied), message = "Bean must not be proxy")
    }

    @Test
    fun `controller method with dto, which has invalid field values, should throw custom runtime exception`() {
        // WHEN
        val proxiedController = getProxiedController()

        // THEN
        assertThrows<InvalidObjectIdFormatException> { proxiedController.create(PassFixture.dtoWithInvalidIdFormats) }
    }

    @Test
    fun `controller method with dto, which has valid field values, should pass`() {
        // GIVEN
        every { passService.create(any(), any(), any()) } returns passFromDb

        // WHEN
        val proxiedController = getProxiedController()

        // THEN
        assertDoesNotThrow { proxiedController.create(dtoWithValidIdFormats) }
    }

    private fun getProxiedController(): PassController {
        return beanPostProcessor.postProcessAfterInitialization(passController, BEAN_NAME) as PassController
    }

    companion object {
        private const val BEAN_NAME = "passController"
    }
}
