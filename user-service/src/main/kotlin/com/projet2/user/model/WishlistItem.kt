package com.projet2.user.model

import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "gameId","platform"])
    ]
)
data class WishlistItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,
    val gameId: String,
    val gameName: String,

    @Column(nullable = false)
    val platform: String
)
