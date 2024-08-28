package com.example.passmanager.configuration

import com.example.passmanager.exception.InvalidIdTypeException
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
class DtoVerifierBeanPostProcessor : BeanPostProcessor {
    private val beanNameToClassMap: MutableMap<String, DtoAnnotatedMethods> = mutableMapOf()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean::class
        if (beanClass.hasAnnotation<RestController>()) {
            val membersWithDtoAsParam = getMethodsWithAnnotatedParams<ValidIdFormat>(bean)
            if (membersWithDtoAsParam.isNotEmpty()) {
                beanNameToClassMap[beanName] = DtoAnnotatedMethods(beanClass.java, membersWithDtoAsParam)
            }
        }
        return bean
    }

    private inline fun <reified T : Annotation> getMethodsWithAnnotatedParams(bean: Any): Set<Method> {
        val beanClass = bean::class.java
        return beanClass.methods.filter {
            it.parameterTypes.any { paramType -> paramType.isAnnotationPresent(T::class.java) }
        }.toSet()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return beanNameToClassMap[beanName]?.let { targetMethods ->
            createEnhancer(bean) { obj, method, args, proxy ->
                if (method in targetMethods.methodsWithDto) {
                    getIdViolationsList(args).takeIf { it.isNotEmpty() }?.let { throw InvalidIdTypeException(it) }
                }
                proxy.invokeSuper(obj, args ?: emptyArray()) // add try logic
            }
        } ?: bean
    }

    private fun getIdViolationsList(args: Array<Any>): List<String> {
        val dtoParams = args.filter { it::class.hasAnnotation<ValidIdFormat>() }
        return dtoParams.flatMap { dto ->
            dto::class.declaredMemberProperties.filter { it.name.contains(ID, ignoreCase = true) }
                .map { field ->
                    field.isAccessible = true
                    field.getter.call(dto)
                }
                .filterIsInstance<String>()
                .filter { !ObjectId.isValid(it) }
        }
    }

    private fun createEnhancer(bean: Any, methodInterceptor: MethodInterceptor): Any {
        val enhancer = Enhancer()
        val beanClass = bean::class

        val primaryConstructorParameters = beanClass.primaryConstructor?.parameters ?: emptyList()
        val argumentTypes = primaryConstructorParameters.map { it.type.jvmErasure.java }.toTypedArray()


        val arguments = primaryConstructorParameters.map { parameter ->
            beanClass.declaredMemberProperties.first { it.name == parameter.name }.let { memberProperty ->
                memberProperty.isAccessible = true
                memberProperty.getter.call(bean)
            }
        }.toTypedArray()

        enhancer.setSuperclass(bean.javaClass)
        enhancer.setCallback(methodInterceptor)
        return enhancer.create(argumentTypes, arguments)
    }


    companion object {
        private const val ID = "Id"
    }

    private data class DtoAnnotatedMethods(val beanClass: Class<*>, val methodsWithDto: Set<Method>)
}

