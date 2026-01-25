package com.example.platform.kafka.consumer;

import com.projet.events.GamePublished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GamePublishedConsumer {

    private static final Logger log = LoggerFactory.getLogger(GamePublishedConsumer.class);

    @KafkaListener(topics = "game-published", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(GamePublished event) {
        log.info("========================================");
        log.info("📥 Événement GamePublished reçu !");
        log.info("   - Jeu : {}", event.getGameName());
        log.info("   - ID : {}", event.getGameId());
        log.info("   - Éditeur : {}", event.getPublisherName());
        log.info("   - Version : {}", event.getVersion());
        log.info("   - Plateforme : {}", event.getPlatform());
        log.info("========================================");
    }
}