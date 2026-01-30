package com.projet2.platform.kafka.producer;

import com.projet2.events.CrashReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer pour envoyer les rapports d'incidents (crashs) vers Kafka.
 * Ces événements seront consommés par l'editor-service pour suivre les problèmes techniques.
 */
@Component
public class GameCrashProducer {

    private static final Logger log = LoggerFactory.getLogger(GameCrashProducer.class);
    private static final String TOPIC = "crash-report";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GameCrashProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envoie un rapport de crash vers Kafka
     *
     * @param event L'événement CrashReport contenant les détails du crash
     */
    public void sendCrashReport(CrashReport event) {
        log.error("🚨 KAFKA : Crash détecté pour '{}' (plateforme: {}) - Envoi rapport à l'éditeur",
                event.getGameName(), event.getPlatform());
        log.error("   Erreur: {}", event.getErrorMessage());

        // Utilisation du gameId comme clé pour partitionner par jeu
        // Cela permet de grouper tous les crashs d'un même jeu sur la même partition
        kafkaTemplate.send(TOPIC, event.getGameId().toString(), event);

        log.info("   ✅ Rapport d'incident envoyé au topic '{}'", TOPIC);
    }

    /**
     * Méthode utilitaire pour créer et envoyer un crash report rapidement
     *
     * @param gameId ID du jeu
     * @param gameName Nom du jeu
     * @param platform Plateforme (PC, PS5, Xbox, etc.)
     * @param errorMessage Message d'erreur
     * @param stackTrace Stack trace complète (peut être null)
     */
    public void reportCrash(String gameId, String gameName, String platform,
                            String errorMessage, String stackTrace) {
        CrashReport event = CrashReport.newBuilder()
                .setGameId(gameId)
                .setGameName(gameName)
                .setPlatform(platform)
                .setErrorMessage(errorMessage)
                .setStackTrace(stackTrace != null ? stackTrace : "No stack trace available")
                .setTimestamp(System.currentTimeMillis())
                .build();

        sendCrashReport(event);
    }
}