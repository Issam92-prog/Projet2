package com.projet2.user.dto

data class RateRequest(
    val gameId: String,
    val note: Int,
    val comment: String
)
