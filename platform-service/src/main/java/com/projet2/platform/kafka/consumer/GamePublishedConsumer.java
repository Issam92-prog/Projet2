package com.projet2.platform.kafka.consumer;

import com.projet2.events.GamePublished;
import com.projet2.platform.entity.Game;
import com.projet2.platform.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GamePublishedConsumer {

    private static final Logger log = LoggerFactory.getLogger(GamePublishedConsumer.class);

    @Autowired
    private GameService gameService;

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

        try {
            Game savedGame = gameService.handleGamePublished(event);
            log.info("✅ Jeu sauvegardé en BDD : {}", savedGame.getTitle());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la sauvegarde : {}", e.getMessage(), e);
        }
    }
}