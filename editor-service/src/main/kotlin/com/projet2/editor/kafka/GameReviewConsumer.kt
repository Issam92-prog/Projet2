package com.projet2.editor.kafka

import com.projet2.editor.domain.GameReview
import com.projet2.editor.domain.ReviewSentiment
import com.projet2.editor.repository.GameReviewRepository
import com.projet2.events.GameReview as GameReviewEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class GameReviewConsumer(
    private val gameReviewRepository: GameReviewRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["game-reviewed"], groupId = "editor-service-group")
    fun consume(record: ConsumerRecord<String, GameReviewEvent>) {
        val event = record.value()

        log.info("⭐ Review reçu pour ${event.gameName} : ${event.rating}/5")

        val sentiment = when {
            event.rating >= 4 -> ReviewSentiment.POSITIVE
            event.rating <= 2 -> ReviewSentiment.NEGATIVE
            else -> ReviewSentiment.NEUTRAL
        }

        // Commentaire problématique si note <= 2 ET mention stabilité
        val isProblematic = event.rating <= 2 &&
                (event.comment?.contains("crash", ignoreCase = true) == true ||
                        event.comment?.contains("bug", ignoreCase = true) == true ||
                        event.comment?.contains("plantage", ignoreCase = true) == true ||
                        event.comment?.contains("instable", ignoreCase = true) == true ||
                        event.comment?.contains("freeze", ignoreCase = true) == true)

        // STOCKAGE EN BASE DE DONNÉES
        val review = GameReview(
            gameId = event.gameId.toString(),
            gameName = event.gameName.toString(),
            userId = event.userId.toString(),
            rating = event.rating,
            comment = event.comment?.toString(),
            reviewedAt = Instant.ofEpochMilli(event.postedAt),
            sentiment = sentiment,
            isProblematic = isProblematic
        )

        gameReviewRepository.save(review)
        log.info("💾 Review sauvegardée (ID: ${review.id})")

        if (isProblematic) {
            log.warn("⚠️ Review problématique détectée pour ${event.gameName} (stabilité)")
        }
    }
}