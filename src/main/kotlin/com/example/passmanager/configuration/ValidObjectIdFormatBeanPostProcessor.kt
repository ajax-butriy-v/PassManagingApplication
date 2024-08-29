package com.example.passmanager.configuration

import com.example.passmanager.exception.InvalidObjectIdFormatException
import org.bson.types.ObjectId
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@Component
internal class ValidObjectIdFormatBeanPostProcessor : BeanPostProcessor {
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
            createEnhancer(bean) { obj, method, args, proxy ->
                if (method in methodsWithAnnotatedParamFields) {
                    val idViolationsList = getIdViolationsList(args)
                    if (idViolationsList.isNotEmpty()) throw InvalidObjectIdFormatException(idViolationsList)
                }
                proxy.invokeSuper(obj, args ?: emptyArray())
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

    private fun getIdViolationsList(args: Array<Any>): List<String> {
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

    private fun createEnhancer(bean: Any, methodInterceptor: MethodInterceptor): Any {
        val beanClass = bean::class

        val primaryConstructorParameters = beanClass.primaryConstructor?.parameters ?: emptyList()
        val argumentTypes = primaryConstructorParameters.map { it.type.jvmErasure.java }.toTypedArray()

        val arguments = primaryConstructorParameters.map { parameter ->
            beanClass.declaredMemberProperties.first { it.name == parameter.name }
                .let { memberProperty ->
                    memberProperty.isAccessible = true
                    memberProperty.getter.call(bean)
                }
        }.toTypedArray()

        val enhancer = Enhancer().apply {
            setSuperclass(bean.javaClass)
            setCallback(methodInterceptor)
        }

        return enhancer.create(argumentTypes, arguments)
    }
}
