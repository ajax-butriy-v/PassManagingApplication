package com.example.passmanagersvc.util

import com.example.passmanagersvc.kafka.configuration.KafkaReactiveConsumerConfiguration
import com.example.passmanagersvc.kafka.configuration.KafkaReactiveProducerConfiguration
import com.example.passmanagersvc.kafka.consumer.TransferPassMessageConsumer
import com.example.passmanagersvc.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.kafka.producer.TransferPassStatisticsMessageProducer
import com.ninjasquad.springmockk.MockkBean

@MockkBean(
    relaxed = true,
    value = [
        TransferPassMessageConsumer::class,
        TransferPassStatisticsMessageProducer::class,
        TransferPassMessageProducer::class,
        KafkaReactiveConsumerConfiguration::class,
        KafkaReactiveProducerConfiguration::class
    ]
)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MockkKafka
