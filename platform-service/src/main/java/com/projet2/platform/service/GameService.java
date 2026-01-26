package com.projet2.platform.service;

import com.projet2.events.GamePublished;
import com.projet2.events.GamePurchased;
import com.projet2.platform.entity.Game;
import com.projet2.platform.kafka.producer.GamePurchasedProducer;
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

    private GameRepository gameRepository;
    private final GamePurchasedProducer gamePurchasedProducer;

    public GameService(GameRepository gameRepository, GamePurchasedProducer gamePurchasedProducer) {
        this.gameRepository = gameRepository;
        this.gamePurchasedProducer = gamePurchasedProducer;
    }

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

        if (event.getPrice() != null) {
            game.setBasePrice(event.getPrice());
            if (game.getCurrentPrice() == null) {
                game.setCurrentPrice(event.getPrice());
            }
        } else {
            // Valeur par défaut si l'event n'a pas de prix
            game.setBasePrice(59.99);
            game.setCurrentPrice(59.99);
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

    // --- ACHAT ---
    public Game buyGame(String gameId, String userId, String platform) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jeu introuvable"));

        // CAS DLC
        if (Boolean.TRUE.equals(game.getIsDlc())) {
            validateDlcRequirements(game, platform);
        } else {
            // CAS JEU STANDARD
            if (!game.getVersions().containsKey(platform)) {
                throw new RuntimeException("Jeu non disponible sur " + platform);
            }
        }

        // Stats et Prix
        game.setSalesCount(game.getSalesCount() + 1);
        updateDynamicPrice(game);

        gameRepository.save(game);

        // Kafka
        GamePurchased event = GamePurchased.newBuilder()
                .setGameId(game.getId())
                .setUserId(userId)
                .setPrice(game.getCurrentPrice())
                .setPlatform(platform)
                .setPurchasedAt(Instant.now().toEpochMilli())
                .build();
        gamePurchasedProducer.sendGamePurchased(event);

        return game;
    }

    //Méthode nécessaire pour mettre à jour les notes (appelée plus tard par un Consumer)
    public void updateRating(String gameId, int stars) {
        gameRepository.findById(gameId).ifPresent(game -> {
            long totalReviews = game.getReviewCount();
            double currentAvg = game.getAverageRating();

            // Formule de moyenne cumulative
            double newAvg = ((currentAvg * totalReviews) + stars) / (totalReviews + 1);

            game.setReviewCount(totalReviews + 1);
            game.setAverageRating(newAvg);

            // Important : On recalcul le prix car la "Qualité perçue" a changé
            updateDynamicPrice(game);

            gameRepository.save(game);
            log.info("⭐ Avis ajouté pour {} : Note {}/5 (Nouvelle moyenne: {})", game.getTitle(), stars, newAvg);
        });
    }

    /**
     * Validation SIMPLIFIÉE pour les DLCs
     */
    private void validateDlcRequirements(Game dlc, String platform) {
        if (dlc.getParentGameId() == null) {
            throw new RuntimeException("Données corrompues : Ce DLC n'a pas de jeu parent.");
        }

        // 1. On cherche le parent
        Game parent = gameRepository.findById(dlc.getParentGameId())
                .orElseThrow(() -> new RuntimeException("Le jeu parent n'existe pas au catalogue."));

        // 2. SEULE VÉRIFICATION : Est-ce que le parent est dispo sur ce support ?
        if (!parent.getVersions().containsKey(platform)) {
            throw new RuntimeException("Impossible d'acheter ce DLC sur " + platform + " car le jeu de base n'y est pas.");
        }

        // Fin de la vérification. Si le parent est là, c'est bon.
    }

    /** calcul du prix selon la demande et les reviews
     *
     * @param game
     */
    public void updateDynamicPrice(Game game) {
        double multiplier = 1.0;

        // Demande
        if (game.getSalesCount() > 1000) multiplier += 0.10;
        else if (game.getSalesCount() < 50) multiplier -= 0.10;

        // Reviews
        if (game.getReviewCount() > 5) {
            if (game.getAverageRating() >= 4.5) multiplier += 0.10;
            else if (game.getAverageRating() < 3.0) multiplier -= 0.20;
        }

        double newPrice = game.getBasePrice() * multiplier;

        // Bornes
        if (newPrice < game.getBasePrice() * 0.5) newPrice = game.getBasePrice() * 0.5;
        if (newPrice > game.getBasePrice() * 1.5) newPrice = game.getBasePrice() * 1.5;

        game.setCurrentPrice(Math.round(newPrice * 100.0) / 100.0);
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