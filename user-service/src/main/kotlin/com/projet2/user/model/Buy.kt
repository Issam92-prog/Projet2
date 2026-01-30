package com.projet2.user.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "gameId", "platform"])
    ]
)
data class Buy(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,
    val gameId: String,
    val gameName: String,

    @Column(nullable = false)
    val platform: String,

    var playTimeHours: Int = 0,
    val price: Double,
    val isDlc: Boolean = false,

    val boughtAt: Instant = Instant.now()
)
