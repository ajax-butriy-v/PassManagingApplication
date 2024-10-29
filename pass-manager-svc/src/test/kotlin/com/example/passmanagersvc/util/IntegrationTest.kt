package com.example.passmanagersvc.util

import com.example.passmanagersvc.kafka.configuration.KafkaReactiveConsumerConfiguration
import com.example.passmanagersvc.kafka.configuration.KafkaReactiveProducerConfig
import com.example.passmanagersvc.kafka.consumer.TransferPassMessageConsumer
import com.example.passmanagersvc.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.kafka.producer.TransferPassStatisticsMessageProducer
import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@MockkBean(
    relaxed = true,
    value = [
        TransferPassMessageConsumer::class,
        TransferPassStatisticsMessageProducer::class,
        TransferPassMessageProducer::class,
        KafkaReactiveConsumerConfiguration::class,
        KafkaReactiveProducerConfig::class
    ]
)
abstract class IntegrationTest
