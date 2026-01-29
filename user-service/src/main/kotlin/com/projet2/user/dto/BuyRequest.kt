package com.projet2.user.dto


data class BuyRequest(
    val gameId: String,
    val gameName: String,
    val platform: String,
    val price: Double,

    // --- NOUVEAUX CHAMPS ---
    val isDlc: Boolean? = false,
    val parentGameId: String? = null // Obligatoire si isDlc = true
)
