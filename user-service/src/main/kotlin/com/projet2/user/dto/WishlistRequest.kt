package com.projet2.user.dto

import com.projet2.user.model.Platform
data class WishlistRequest(
    val gameId: String,
    val gameName: String,
    val platform: Platform
)
