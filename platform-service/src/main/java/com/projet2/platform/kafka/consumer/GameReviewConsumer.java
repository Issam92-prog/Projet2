package com.projet2.platform.kafka.consumer;

import com.projet2.events.GameReview;
import com.projet2.platform.entity.Game;
import com.projet2.platform.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GameReviewConsumer {

    private static final Logger log = LoggerFactory.getLogger(GameReviewConsumer.class);

    private final GameService gameService;

    public GameReviewConsumer(GameService gameService) {
        this.gameService = gameService;
    }

    @KafkaListener(topics = "game-reviewed", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(GameReview event) {
        // 1. On récupère les infos du jeu en base pour enrichir le log
        Optional<Game> gameOpt = gameService.getGameById(event.getGameId().toString());

        String type = "Jeu inconnu";

        if (gameOpt.isPresent()) {
            Game game = gameOpt.get();
            // On détermine le type d'après le booléen en base
            type = Boolean.TRUE.equals(game.getIsDlc()) ? "DLC" : "JEU";
        }

        // 2. Log enrichi : on sait maintenant ce que c'est !
        log.info("⭐ REVIEW REÇUE : [{}] '{}' noté {}/5 par User {}",
                type, event.getGameName(), event.getRating(), event.getUserId());

        try {
            // 3. Mise à jour de la note et du prix
            gameService.updateRating(event.getGameId().toString(), event.getRating());
        } catch (Exception e) {
            log.error("❌ Erreur traitement review : {}", e.getMessage());
        }
    }
}