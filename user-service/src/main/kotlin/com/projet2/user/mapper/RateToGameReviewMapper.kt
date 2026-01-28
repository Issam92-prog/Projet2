package com.projet2.user.mapper

import com.projet2.user.model.Rate
import com.projet2.events.GameReview

object RateToGameReviewMapper {

    fun map(rate: Rate): GameReview {
        return GameReview(
            rate.gameId,
            rate.gameName,
            rate.userId.toString(),
            rate.note,
            rate.comment,
            rate.ratedAt.toEpochMilli()
        )

    }
}