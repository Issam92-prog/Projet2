package com.projet2.platform.service;

import com.projet2.events.GamePublished;
import com.projet2.platform.entity.Game;
import com.projet2.platform.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameRepository gameRepository;

    /**
     * Appelé quand un éditeur publie un nouveau jeu (GamePublishedConsumer).
     */
    public Game handleGamePublished(GamePublished event) {
        log.info("💾 Traitement de GamePublished : {}", event.getGameName());

        Optional<Game> existingGame = gameRepository.findById(event.getGameId().toString());

        Game game;
        if (existingGame.isPresent()) {
            game = existingGame.get();
            log.info("   ↻ Mise à jour du jeu existant");
        } else {
            game = new Game();
            game.setId(event.getGameId().toString());
            // Sécurité pour la Map
            if (game.getVersions() == null) {
                game.setVersions(new HashMap<>());
            }
            game.setCreatedAt(Instant.ofEpochMilli(event.getPublishedAt()));
            log.info("   ✨ Création d'un nouveau jeu");
        }

        // Mapping des champs simples
        game.setTitle(event.getGameName().toString());
        game.setPublisherName(event.getPublisherName().toString());
        game.setIsEarlyAccess(false);

        // --- LOGIQUE MULTI-PLATEFORME ---
        // On AJOUTE la plateforme reçue dans la map des versions
        String platform = event.getPlatform().toString();
        String version = event.getVersion().toString();

        game.getVersions().put(platform, version);
        // --------------------------------

        // Mapping des genres
        if (event.getGenre() != null) {
            List<String> genres = event.getGenre().stream()
                    .map(CharSequence::toString)
                    .collect(Collectors.toList());
            game.setGenres(genres);
        }

        Game savedGame = gameRepository.save(game);
        log.info("   ✅ Jeu sauvegardé : {} - Ajout Support {} (v{})", savedGame.getTitle(), platform, version);

        return savedGame;
    }

    /**
     * Appelé quand un éditeur sort un patch (PatchReleasedConsumer).
     */
    public void applyPatch(String gameId, String platform, String newVersion, String comment) {
        // 1. Chercher le jeu
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (gameOpt.isEmpty()) {
            log.warn("⛔ IGNORE PATCH : Jeu inconnu au catalogue (ID: {}).", gameId);
            return;
        }

        Game game = gameOpt.get();

        // 2. Vérifier si on possède cette plateforme
        if (!game.getVersions().containsKey(platform)) {
            log.warn("⛔ IGNORE PATCH : Le jeu '{}' n'est pas référencé sur {} chez nous.", game.getTitle(), platform);
            return;
        }

        String currentVersion = game.getVersions().get(platform);

        // 3. Vérifier l'antériorité (Est-ce vraiment une mise à jour ?)
        if (isVersionNewer(currentVersion, newVersion)) {
            log.info("✅ PATCH APPLIQUÉ : {} sur {} passe de {} à {}.", game.getTitle(), platform, currentVersion, newVersion);

            // Mise à jour de la version
            game.getVersions().put(platform, newVersion);

            gameRepository.save(game);

            log.info("   -> Commentaire patch : {}", comment);
        } else {
            log.warn("⚠️ PATCH OBSOLÈTE : Version reçue ({}) <= Version actuelle ({}).", newVersion, currentVersion);
        }
    }

    // Méthode utilitaire simple pour comparer "1.5" et "1.2"
    private boolean isVersionNewer(String current, String target) {
        if (current == null) return true;
        return target.compareTo(current) > 0;
    }

    // --- Méthodes de lecture ---

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(String id) {
        return gameRepository.findById(id);
    }

    public List<Game> getGamesByPlatform(String platform) {
        return gameRepository.findByPlatform(platform);
    }

    public List<Game> getGamesByPublisher(String publisherName) {
        return gameRepository.findByPublisherName(publisherName);
    }
}