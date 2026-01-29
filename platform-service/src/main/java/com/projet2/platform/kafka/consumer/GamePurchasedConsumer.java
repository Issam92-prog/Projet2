package com.projet2.platform.kafka.consumer;

import com.projet2.events.GamePurchased;
import com.projet2.platform.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GamePurchasedConsumer {

    private final Logger log = LoggerFactory.getLogger(GamePurchasedConsumer.class);
    private final GameService gameService;

    public GamePurchasedConsumer(GameService gameService) {
        this.gameService = gameService;
    }

    @KafkaListener(topics = "game-purchased", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(GamePurchased event) {
        log.info("💰 Achat détecté : {} a acheté {}", event.getUserId(), event.getGameName());

        try {
            // On met à jour les stats du jeu (Ventes + Prix)
            gameService.processSale(event.getGameId().toString());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des stats : {}", e.getMessage());
        }
    }
}