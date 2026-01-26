package com.projet2.user.dto

import java.time.LocalDate

data class CreateUserRequest(
    val pseudo: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate
)
