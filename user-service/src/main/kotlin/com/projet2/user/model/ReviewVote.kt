package com.projet2.user.model

import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "rateId"])
    ]
)
data class ReviewVote(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,
    val rateId: Long,

    val useful: Boolean
)
