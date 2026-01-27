package com.projet2.platform.kafka.producer;

import com.projet2.events.GamePurchased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class GamePurchasedProducer {

    private static final Logger log = LoggerFactory.getLogger(GamePurchasedProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GamePurchasedProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendGamePurchased(GamePurchased event) {
        log.info("📤 KAFKA : Achat validé -> Envoi event pour '{}' (User: {})", event.getGameName(), event.getUserId());

        // On utilise gameId comme clé pour garder l'ordre si besoin,
        // ou userId si on veut grouper par utilisateur. Ici gameId est pertinent pour les stats du jeu.
        kafkaTemplate.send("game-purchased", event.getGameId().toString(), event);
    }
}