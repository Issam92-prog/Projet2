package com.projet2.editor.dto

data class IncidentDTO(
    val id: Long?,
    val gameId: String,
    val gameName: String,
    val platform: String,
    val errorMessage: String,
    val severity: String,
    val status: String,
    val reportedAt: String
)

data class IncidentCountResponse(
    val gameId: String,
    val unprocessedIncidents: Long
)