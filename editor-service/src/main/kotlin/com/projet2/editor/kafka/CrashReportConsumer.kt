package com.projet2.editor.kafka

import com.projet2.editor.domain.ErrorDetails
import com.projet2.editor.domain.ErrorSeverity
import com.projet2.editor.domain.IncidentReport
import com.projet2.editor.domain.IncidentStatus
import com.projet2.editor.repository.IncidentReportRepository
import com.projet2.events.CrashReport
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
    fun consume(event: CrashReport) {
        log.warn("Crash reçu pour le jeu : ${event.gameName}")

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

        incidentReportRepository.save(incident)
        log.info("💾 Incident sauvegardé (ID: ${incident.id})")

        val unprocessedCount = incidentReportRepository
            .countByGameIdAndStatus(event.gameId.toString(), IncidentStatus.NEW)

        if (unprocessedCount >= 10) {
            log.error("⚠️ Seuil critique atteint pour ${event.gameName} : $unprocessedCount crashs non traités !")
            // TODO: Appeler PatchService pour créer un patch automatique
        }
    }

    /**
     * Détermine la sévérité en fonction du message d'erreur
     */
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