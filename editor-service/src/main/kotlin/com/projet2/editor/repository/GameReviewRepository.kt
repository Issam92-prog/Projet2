package com.projet2.editor.repository

import com.projet2.editor.domain.GameReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameReviewRepository : JpaRepository<GameReview, Long> {
    fun findByGameId(gameId: String): List<GameReview>
    fun findByIsProblematicTrue(): List<GameReview>
    fun countByGameIdAndRatingLessThan(gameId: String, rating: Int): Long
}