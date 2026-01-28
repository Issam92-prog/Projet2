package com.projet2.user.mapper

import com.projet2.user.model.Buy
import com.projet2.events.GamePurchased

object BuyToGamePurchasedMapper {

    fun map(buy: Buy): GamePurchased {
        return GamePurchased(
            buy.gameId,                     // gameId
            buy.gameName,                   // gameName
            buy.userId.toString(),          // userId
            buy.price,                      // price
            buy.platform.name,              // platform
            buy.boughtAt.toEpochMilli()     // purchasedAt
        )
    }
}
