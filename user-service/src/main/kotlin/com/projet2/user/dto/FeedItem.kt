package com.projet2.user.dto

import java.time.Instant

data class FeedItem(
    val type: FeedType,
    val gameId: String,
    val gameName: String,
    val message: String,
    val createdAt: Instant,
    //valeur par défaut 0 pour ne pas casser le reste
    val usefulCount: Long = 0,
    val uselessCount: Long = 0
)

enum class FeedType {
    NEW_REVIEW,
    NEW_DLC,
    PRICE_DROP,
    WISHLIST_HINT
}
