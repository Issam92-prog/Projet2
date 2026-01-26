package com.projet2.user.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["pseudo"])
    ]
)
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val pseudo: String,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Column(nullable = false)
    val birthDate: LocalDate,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
