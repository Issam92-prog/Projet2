package com.projet2.editor.controller

import com.projet2.editor.domain.IncidentStatus
import com.projet2.editor.dto.IncidentDTO
import com.projet2.editor.dto.IncidentCountResponse
import com.projet2.editor.repository.IncidentReportRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/incidents")
class IncidentController(
    private val incidentReportRepository: IncidentReportRepository
) {

    @GetMapping
    fun getAllIncidents(
        @RequestParam(required = false) gameId: String?
    ): List<IncidentDTO> {
        val incidents = if (gameId != null) {
            incidentReportRepository.findByGameIdAndStatus(gameId, IncidentStatus.NEW)
        } else {
            incidentReportRepository.findAll()
        }

        return incidents.map {
            IncidentDTO(
                id = it.id,
                gameId = it.gameId,
                gameName = it.gameName,
                platform = it.platform,
                errorMessage = it.error.message,
                severity = it.error.severity.name,
                status = it.status.name,
                reportedAt = it.reportedAt.toString()
            )
        }
    }

    @GetMapping("/count")
    fun countIncidents(@RequestParam gameId: String): IncidentCountResponse {
        val count = incidentReportRepository.countByGameIdAndStatus(gameId, IncidentStatus.NEW)
        return IncidentCountResponse(gameId = gameId, unprocessedIncidents = count)
    }
}