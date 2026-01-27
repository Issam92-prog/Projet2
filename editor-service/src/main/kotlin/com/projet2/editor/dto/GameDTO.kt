package com.projet2.editor.dto

data class GameDTO(
    val id: Long?,
    val gameId: String,
    val name: String,
    val platform: String,
    val genre: String,
    val currentVersion: String,
    val publisherName: String
)