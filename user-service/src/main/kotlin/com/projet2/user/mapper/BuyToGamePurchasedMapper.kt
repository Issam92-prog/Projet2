package com.projet2.user.mapper

import com.projet2.user.model.Buy
import com.projet2.events.GamePurchased

object BuyToGamePurchasedMapper {

    fun map(buy: Buy): GamePurchased {
        return GamePurchased.newBuilder()
            .setGameId(buy.gameId)
            .setUserId(buy.userId.toString())

            .setGameName(buy.gameName)

            .setPrice(buy.price)
            .setPlatform(buy.platform.toString())
            .setPurchasedAt(buy.boughtAt.toEpochMilli())
            .build()
    }
}