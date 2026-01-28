package com.projet2.user.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true) var pseudo: String,

    @Column(nullable = false) var firstName: String,

    @Column(nullable = false) var lastName: String,

    @Column(nullable = false) var birthDate: LocalDate,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false, unique = true) var email: String,

    @Column(nullable = false) var password: String
)

