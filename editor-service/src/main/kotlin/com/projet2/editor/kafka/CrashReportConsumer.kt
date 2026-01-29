package com.projet2.editor.kafka

import com.projet2.editor.domain.ErrorDetails
import com.projet2.editor.domain.ErrorSeverity
import com.projet2.editor.domain.IncidentReport
import com.projet2.editor.domain.IncidentStatus
import com.projet2.editor.repository.IncidentReportRepository
import com.projet2.events.CrashReport
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CrashReportConsumer(
    private val incidentReportRepository: IncidentReportRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["crash-report"], groupId = "editor-service-group")
    fun consume(record: ConsumerRecord<String, CrashReport>) {
        val event = record.value()

        log.warn("===============================================")
        log.warn("🔥 CONSUMER KAFKA APPELÉ !")
        log.warn("Game ID: ${event.gameId}")
        log.warn("Game Name: ${event.gameName}")
        log.warn("Error: ${event.errorMessage}")
        log.warn("===============================================")

        try {
            val errorDetails = ErrorDetails(
                message = event.errorMessage.toString(),
                stackTrace = event.stackTrace?.toString(),
                severity = determineSeverity(event.errorMessage.toString())
            )

            val incident = IncidentReport(
                gameId = event.gameId.toString(),
                gameName = event.gameName.toString(),
                platform = event.platform.toString(),
                error = errorDetails,
                reportedAt = Instant.ofEpochMilli(event.timestamp),
                status = IncidentStatus.NEW
            )

            val saved = incidentReportRepository.save(incident)
            log.warn("💾 INCIDENT SAUVEGARDÉ (ID: ${saved.id}) !")

            val unprocessedCount = incidentReportRepository
                .countByGameIdAndStatus(event.gameId.toString(), IncidentStatus.NEW)

            log.warn("📊 Total incidents non traités pour ce jeu : $unprocessedCount")

            if (unprocessedCount >= 10) {
                log.error("⚠️⚠ SEUIL CRITIQUE ATTEINT : $unprocessedCount crashs !")
            }
        } catch (e: Exception) {
            log.error("❌ ERREUR DANS LE CONSUMER : ${e.message}", e)
        }
    }

    private fun determineSeverity(errorMessage: String): ErrorSeverity {
        return when {
            errorMessage.contains("NullPointerException", ignoreCase = true) -> ErrorSeverity.HIGH
            errorMessage.contains("OutOfMemory", ignoreCase = true) -> ErrorSeverity.CRITICAL
            errorMessage.contains("Fatal", ignoreCase = true) -> ErrorSeverity.CRITICAL
            errorMessage.contains("Error", ignoreCase = true) -> ErrorSeverity.MEDIUM
            else -> ErrorSeverity.LOW
        }
    }
}