package com.example.gateway.infrastructure.bpp

import com.example.core.exception.InvalidObjectIdFormatException
import org.bson.types.ObjectId
import org.springframework.aop.MethodBeforeAdvice
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method
import kotlin.reflect.full.hasAnnotation

@Component
class ValidObjectIdFormatBeanPostProcessor : BeanPostProcessor {
    private val beanMap: MutableMap<String, Set<Method>> = mutableMapOf()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean::class
        if (beanClass.hasAnnotation<RestController>()) {
            val methodWithAnnotatedFields = getMethodsWithAnnotatedParamFields<ValidObjectIdFormat>(bean)
            if (methodWithAnnotatedFields.isNotEmpty()) {
                beanMap[beanName] = methodWithAnnotatedFields
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return beanMap[beanName]?.let { methodsWithAnnotatedParamFields ->
            createEnhancer(bean) { method, args, _ ->
                if (method in methodsWithAnnotatedParamFields) {
                    val idViolationsList = getIdViolationsList(args)
                    if (idViolationsList.isNotEmpty()) throw InvalidObjectIdFormatException(idViolationsList)
                }
            }
        } ?: bean
    }

    private inline fun <reified T : Annotation> getMethodsWithAnnotatedParamFields(bean: Any): Set<Method> {
        val beanClass = bean::class.java
        return beanClass.methods.filterTo(mutableSetOf()) { method ->
            val fields = method.parameterTypes.flatMap { it.declaredFields.toList() }
            fields.any { it.isAnnotationPresent(T::class.java) }
        }
    }

    private fun getIdViolationsList(args: Array<out Any>): List<String> {
        val onlyDtosWithAnnotatedFields = args.filter {
            it.javaClass.declaredFields.any { field -> field.isAnnotationPresent(ValidObjectIdFormat::class.java) }
        }
        return onlyDtosWithAnnotatedFields.flatMap { dto -> getIdFieldsValues(dto).filter { !ObjectId.isValid(it) } }
    }

    private fun getIdFieldsValues(dto: Any): List<String> {
        return dto.javaClass.declaredFields
            .filter { it.isAnnotationPresent(ValidObjectIdFormat::class.java) }
            .map { field ->
                field.isAccessible = true
                field.get(dto)
            }
            .filterIsInstance<String>()
    }

    private fun createEnhancer(bean: Any, methodBeforeAdvice: MethodBeforeAdvice): Any {
        val proxyFactory = ProxyFactory().apply {
            setTarget(bean)
            addAdvice(methodBeforeAdvice)
        }

        return proxyFactory.proxy
    }
}
