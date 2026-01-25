package com.projet2.platform.controller;

import com.projet2.platform.test.TestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de test pour envoyer des événements manuellement
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestProducer testProducer;

    /**
     * POST /api/test/publish-game
     * Envoie un événement GamePublished de test
     */
    @PostMapping("/publish-game")
    public ResponseEntity<Map<String, String>> publishTestGame() {
        testProducer.sendTestGame();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Événement GamePublished envoyé à Kafka"
        ));
    }
}