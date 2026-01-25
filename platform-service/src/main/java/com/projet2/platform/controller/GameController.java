package com.projet2.platform.controller;

import com.projet2.platform.entity.Game;
import com.projet2.platform.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller REST pour consulter les jeux du catalogue
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * GET /api/games
     * Récupère tous les jeux
     */
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    /**
     * GET /api/games/{id}
     * Récupère un jeu par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable String id) {
        Optional<Game> game = gameService.getGameById(id);
        return game.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/games/platform/{platform}
     * Récupère les jeux d'une plateforme
     */
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<Game>> getGamesByPlatform(@PathVariable String platform) {
        List<Game> games = gameService.getGamesByPlatform(platform);
        return ResponseEntity.ok(games);
    }

    /**
     * GET /api/games/publisher/{publisherName}
     * Récupère les jeux d'un éditeur
     */
    @GetMapping("/publisher/{publisherName}")
    public ResponseEntity<List<Game>> getGamesByPublisher(@PathVariable String publisherName) {
        List<Game> games = gameService.getGamesByPublisher(publisherName);
        return ResponseEntity.ok(games);
    }

    /**
     * GET /api/games/count
     * Compte le nombre total de jeux
     */
    @GetMapping(value = "/count", produces = "text/plain")
    public ResponseEntity<String> countGames() {
        long count = gameService.getAllGames().size();
        return ResponseEntity.ok("Nombre de jeux :" + count+ "\n");
    }
}