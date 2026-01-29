package com.projet2.editor.config

import com.projet2.events.CrashReport
import com.projet2.events.GamePublished
import com.projet2.events.PatchReleased
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.properties.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    private fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            "schema.registry.url" to schemaRegistryUrl,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3
        )
    }

    @Bean
    fun gamePublishedKafkaTemplate(): KafkaTemplate<String, GamePublished> {
        val factory: ProducerFactory<String, GamePublished> =
            DefaultKafkaProducerFactory(producerConfigs())
        return KafkaTemplate(factory)
    }

    @Bean
    fun patchReleasedKafkaTemplate(): KafkaTemplate<String, PatchReleased> {
        val factory: ProducerFactory<String, PatchReleased> =
            DefaultKafkaProducerFactory(producerConfigs())
        return KafkaTemplate(factory)
    }

    @Bean
    fun crashReportKafkaTemplate(): KafkaTemplate<String, CrashReport> {
        val factory: ProducerFactory<String, CrashReport> =
            DefaultKafkaProducerFactory(producerConfigs())
        return KafkaTemplate(factory)
    }
}