package com.projet2.user.model

import jakarta.persistence.*
//description de l'extension
@Entity
data class Dlc(

    @Id
    val id: String,          // ex: "dlc-001"

    val gameId: String,      // jeu de base
    val name: String
)
