package com.projet2.user.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    fun publish(topic: String, key: String, event: Any) {
        kafkaTemplate.send(topic, key, event)
    }
}
