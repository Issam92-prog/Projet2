package com.projet2.platform;

import com.projet2.events.GamePublished;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GameConsumerService {

    @KafkaListener(topics = "game-published", groupId = "platform-main-group")
    public void consume(GamePublished game) {
        System.out.println("PLATFORME : Jeu reçu -> " + game.getGameName() + " (" + game.getPublisherName() + ")");
    }
}