package com.projet2.platform.test;

import com.projet2.events.GamePublished;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
public class TestProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;  // ← Object au lieu de GamePublished

    private static final String TOPIC = "game-published";

    public void sendTestGame() {
        GamePublished event = GamePublished.newBuilder()
                .setGameId(UUID.randomUUID().toString())
                .setGameName("Grand Theft Auto VI")
                .setPlatform("PS5")
                .setGenre(Arrays.asList("Action", "Adventure", "Open World"))
                .setVersion("1.0.0")
                .setPublisherName("Rockstar Games")
                .setPublishedAt(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(TOPIC, event.getGameId().toString(), event);

        System.out.println("✅ Événement envoyé : " + event.getGameName());
        System.out.println("   - Plateforme : " + event.getPlatform());
        System.out.println("   - Éditeur : " + event.getPublisherName());
        System.out.println("   - Genres : " + event.getGenre());
    }
}