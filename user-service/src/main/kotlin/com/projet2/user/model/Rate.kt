package com.projet2.user.model

import jakarta.persistence.*
import java.time.Instant

@Entity
data class Rate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,
    val gameId: String,
    val note: Int,
    val comment: String,

    val ratedAt: Instant = Instant.now()
)
