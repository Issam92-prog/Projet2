package com.projet2.editor.dto.request

data class CreatePatchRequest(
    val gameId: String,
    val newVersion: String,
    val description: String,
    val changes: List<String>,
    val type: String = "BUGFIX"
)