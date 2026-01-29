package com.projet2.user.dto

data class UserProfileDTO(
    val userId: String,
    val username: String,
    val joinedAt: String,
    val email: String? = null,
    val totalPlayTime: Int? = null, // Ou Long selon ton modèle
    val gamesOwned: List<GameSummary>? = null,
    val reviews: List<ReviewSummary>? = null
)

data class GameSummary(
    val gameName: String,
    val playTimeHours: Int // Ou Long
)

data class ReviewSummary(
    val gameId: String,
    val stars: Int,
    val comment: String
)