package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "incident_reports", indexes = [
    Index(name = "idx_game_status", columnList = "game_id,is_processed")
])
data class IncidentReport(
    @Column(name = "game_id", nullable = false)
    val gameId: String,

    @Column(name = "game_name")
    val gameName: String,

    @Column(nullable = false)
    val platform: String,

    @Embedded
    val error: ErrorDetails,

    @Column(name = "reported_at")
    val reportedAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: IncidentStatus = IncidentStatus.NEW,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    fun markAsProcessed() {
        status = IncidentStatus.PROCESSED
    }

    fun isUnprocessed() = status == IncidentStatus.NEW
}

@Embeddable
data class ErrorDetails(
    @Column(name = "error_message", columnDefinition = "TEXT", nullable = false)
    val message: String,

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    val stackTrace: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    val severity: ErrorSeverity = ErrorSeverity.MEDIUM
)

enum class IncidentStatus {
    NEW,
    PROCESSING,
    PROCESSED,
    IGNORED
}

enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}