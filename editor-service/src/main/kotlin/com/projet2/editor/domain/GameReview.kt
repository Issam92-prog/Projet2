package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "game_reviews", indexes = [
    Index(name = "idx_game_review", columnList = "game_id"),
    Index(name = "idx_problematic", columnList = "is_problematic")
])
data class GameReview(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "game_id", nullable = false)
    val gameId: String,

    @Column(name = "game_name")
    val gameName: String,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(nullable = false)
    val rating: Int, // 1-5

    @Column(columnDefinition = "TEXT")
    val comment: String?,

    @Column(name = "reviewed_at")
    val reviewedAt: Instant,

    @Enumerated(EnumType.STRING)
    val sentiment: ReviewSentiment = ReviewSentiment.NEUTRAL,

    @Column(name = "is_problematic")
    val isProblematic: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

enum class ReviewSentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}