package com.projet2.platform.controller;

import com.projet2.platform.service.GameMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour tester et déclencher des rapports de crash
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private GameMonitoringService monitoringService;

    /**
     * POST /api/monitoring/crash/{gameId}
     * Simule un crash pour un jeu spécifique
     *
     * Exemple: POST http://localhost:8080/api/monitoring/crash/abc-123
     */
    @PostMapping("/crash/{gameId}")
    public ResponseEntity<Map<String, String>> simulateCrash(@PathVariable String gameId) {
        try {
            monitoringService.simulateCrash(gameId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Crash simulé et rapport envoyé pour le jeu " + gameId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/monitoring/crash/random?count=5
     * Simule plusieurs crashs aléatoires
     *
     * Exemple: POST http://localhost:8080/api/monitoring/crash/random?count=10
     */
    @PostMapping("/crash/random")
    public ResponseEntity<Map<String, String>> simulateRandomCrashes(
            @RequestParam(defaultValue = "5") int count) {
        try {
            monitoringService.simulateRandomCrashes(count);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", count + " crashs aléatoires simulés et rapports envoyés"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/monitoring/crash/custom
     * Simule un crash personnalisé avec tous les détails
     *
     * Body JSON:
     * {
     *   "gameId": "abc-123",
     *   "platform": "PC",
     *   "errorMessage": "NullPointerException in render loop",
     *   "stackTrace": "at com.game.Engine.render(...)"
     * }
     */
    @PostMapping("/crash/custom")
    public ResponseEntity<Map<String, String>> reportCustomCrash(
            @RequestBody CrashRequest request) {
        try {
            monitoringService.reportCustomCrash(
                    request.gameId(),
                    request.platform(),
                    request.errorMessage(),
                    request.stackTrace()
            );
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Crash report personnalisé envoyé"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Record pour la requête de crash personnalisé
     */
    record CrashRequest(
            String gameId,
            String platform,
            String errorMessage,
            String stackTrace
    ) {}
}