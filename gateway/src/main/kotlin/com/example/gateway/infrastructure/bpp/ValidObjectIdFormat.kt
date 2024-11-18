package com.example.gateway.infrastructure.bpp

/**
 * Annotation used to validate that a `String` field can be converted to a valid `ObjectId` instance.
 *
 * This annotation should be applied only to `String` fields. When a field is annotated with `@ValidObjectIdFormat`,
 * the `ValidObjectIdFormatBeanPostProcessor` class will automatically check if the value of this `String` field can
 * be converted to a valid `ObjectId`. If the value cannot be converted, a `InvalidObjectIdFormatException`
 * will be thrown, resulting in a response with a _400 Bad Request_ status code.
 *
 * @see ValidObjectIdFormatBeanPostProcessor
 */

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidObjectIdFormat
