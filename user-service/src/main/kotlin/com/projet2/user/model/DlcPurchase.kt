package com.projet2.user.model

import jakarta.persistence.*
import java.time.Instant
//licence par dlc
@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "dlcId"])
    ]
)
data class DlcPurchase(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,
    val dlcId: String,
    val gameId: String,

    val purchasedAt: Instant = Instant.now()
)
