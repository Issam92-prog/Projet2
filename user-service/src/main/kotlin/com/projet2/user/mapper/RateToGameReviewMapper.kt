package com.projet2.user.mapper

import com.projet2.user.model.Rate
import com.projet2.events.GameReview
import java.time.Instant

object RateToGameReviewMapper {

    fun map(rate: Rate): GameReview {
        return GameReview.newBuilder()
            .setReviewId(rate.id.toString())
            .setUserId(rate.userId.toString())
            .setGameId(rate.gameId)
            .setGameName(rate.gameName)
            .setRating(rate.note)
            .setComment(rate.comment)
            .setPostedAt(Instant.now().toEpochMilli())
            .build()

    }
}