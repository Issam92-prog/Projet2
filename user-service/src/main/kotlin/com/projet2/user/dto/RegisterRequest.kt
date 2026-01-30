package com.projet2.user.dto

import java.time.LocalDate

data class RegisterRequest(
    val pseudo: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate,
    val email: String,
    val password: String
)
