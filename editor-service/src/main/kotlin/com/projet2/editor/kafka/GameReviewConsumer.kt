package com.projet2.editor.kafka

import com.projet2.events.GameReview
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GameReviewConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["game-review"], groupId = "editor-service-group")
    fun consume(record: ConsumerRecord<String, GameReview>) {
        val event = record.value()
        log.info("⭐ Review reçu pour ${event.gameName} : ${event.rating}/5")

        // Routage spécial pour les mauvaises notes
        if (event.rating <= 2) {
            log.warn("⚠️ Mauvaise évaluation détectée !")
            log.warn("   Jeu : ${event.gameName}")
            log.warn("   Note : ${event.rating}/5")
            log.warn("   Commentaire : ${event.comment}")
            log.warn("   Utilisateur : ${event.userId}")

            // TODO: Stocker dans une table spéciale ou envoyer une alerte
            // Exemple : créer une notification pour l'équipe de dev
        }

        // TODO: Agréger les évaluations pour calculer la note moyenne
    }
}