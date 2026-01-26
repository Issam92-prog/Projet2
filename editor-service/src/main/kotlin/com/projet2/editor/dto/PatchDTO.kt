package com.projet2.editor.dto

data class PatchDTO(
    val id: Long?,
    val newVersion: String,
    val previousVersion: String,
    val description: String,
    val type: String,
    val changes: List<String>,
    val releasedAt: String
)