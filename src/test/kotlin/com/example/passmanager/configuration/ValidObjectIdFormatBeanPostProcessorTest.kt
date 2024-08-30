package com.example.passmanager.configuration

import com.example.passmanager.exception.InvalidObjectIdFormatException
import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture
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
        val beanAfterProcessing = beanPostProcessor.postProcessAfterInitialization(passController, BEAN_NAME)
        assertTrue(AopUtils.isCglibProxy(beanAfterProcessing))
    }

    @Test
    fun `processing bean, which not match BPP logic, must return bean`() {
        val beanNotToBeProxied = beanPostProcessor.postProcessAfterInitialization(passService, "passService")
        assertThat(beanNotToBeProxied).isEqualTo(passService)
        assertFalse(AopUtils.isCglibProxy(beanNotToBeProxied))
    }

    @Test
    fun `controoledto with invalid fields values should throw custom e`() {
        val proxiedController = getProxiedController()
        assertThrows<InvalidObjectIdFormatException> { proxiedController.create(PassFixture.dtoWithInvalidIdFormats) }
    }

    @Test
    fun `controller method with dto, that have valid values, should pass`() {
        every { passService.create(any(), any(), any()) } returns passFromDb
        val proxiedController = getProxiedController()
        assertDoesNotThrow { proxiedController.create(PassFixture.dtoWithValidIdFormats) }
    }

    private fun getProxiedController(): PassController {
        return beanPostProcessor.postProcessAfterInitialization(passController, BEAN_NAME) as PassController
    }

    companion object {
        private const val BEAN_NAME = "passController"
    }
}
