package com.projet2.editor.repository

import com.projet2.editor.domain.ErrorSeverity
import com.projet2.editor.domain.IncidentReport
import com.projet2.editor.domain.IncidentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface IncidentReportRepository : JpaRepository<IncidentReport, Long> {

    fun findByGameIdAndStatus(gameId: String, status: IncidentStatus): List<IncidentReport>

    fun countByGameIdAndStatus(gameId: String, status: IncidentStatus): Long

    @Query("SELECT i FROM IncidentReport i WHERE i.gameId = :gameId AND i.status = 'NEW'")
    fun findUnprocessedByGameId(@Param("gameId") gameId: String): List<IncidentReport>

    @Query("SELECT i FROM IncidentReport i WHERE i.error.severity = :severity AND i.status = 'NEW'")
    fun findBySeverityAndUnprocessed(@Param("severity") severity: ErrorSeverity): List<IncidentReport>

    @Query("""
        SELECT i.gameId as gameId, COUNT(i) as count 
        FROM IncidentReport i 
        WHERE i.status = 'NEW' AND i.reportedAt > :since
        GROUP BY i.gameId 
        HAVING COUNT(i) > :threshold
        ORDER BY COUNT(i) DESC
    """)
    fun findGamesWithHighIncidentRate(
        @Param("since") since: Instant,
        @Param("threshold") threshold: Long
    ): List<GameIncidentStats>

    @Query("""
        SELECT i FROM IncidentReport i 
        WHERE i.error.severity = 'CRITICAL' 
        AND i.status = 'NEW' 
        ORDER BY i.reportedAt DESC
    """)
    fun findCriticalUnprocessedIncidents(): List<IncidentReport>

    @Query("SELECT i.platform as platform, COUNT(i) as count FROM IncidentReport i GROUP BY i.platform")
    fun countByPlatform(): List<PlatformIncidentCount>

    @Query("""
        SELECT i FROM IncidentReport i 
        WHERE i.gameId = :gameId 
        AND i.reportedAt BETWEEN :startDate AND :endDate
        ORDER BY i.reportedAt DESC
    """)
    fun findByGameIdAndDateRange(
        @Param("gameId") gameId: String,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<IncidentReport>

    @Query("UPDATE IncidentReport i SET i.status = 'PROCESSED' WHERE i.gameId = :gameId AND i.status = 'NEW'")
    fun markAllAsProcessedByGameId(@Param("gameId") gameId: String): Int
}