package com.projet2.platform.service;

import com.projet2.events.GamePublished;
import com.projet2.platform.entity.Game;
import com.projet2.platform.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final GameRepository gameRepository;

    // CORRECTION : On a supprimé le Consumer (et le Producer) du constructeur
    // Le Service n'a besoin que du Repository.
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Appelé quand un éditeur publie un nouveau jeu.
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
            game.setBasePrice(59.99);
            game.setCurrentPrice(59.99);
        }

        game.setTitle(event.getGameName().toString());
        game.setPublisherName(event.getPublisherName().toString());
        game.setIsEarlyAccess(false);

        String platform = event.getPlatform().toString();
        String version = event.getVersion().toString();
        game.getVersions().put(platform, version);

        if (event.getGenre() != null) {
            List<String> genres = event.getGenre().stream()
                    .map(CharSequence::toString)
                    .collect(Collectors.toList());
            game.setGenres(genres);
        }

        return gameRepository.save(game);
    }

    public void applyPatch(String gameId, String platform, String newVersion, String comment) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) return;

        Game game = gameOpt.get();
        if (!game.getVersions().containsKey(platform)) return;

        String currentVersion = game.getVersions().get(platform);

        if (isVersionNewer(currentVersion, newVersion)) {
            game.getVersions().put(platform, newVersion);
            gameRepository.save(game);
            log.info("✅ PATCH APPLIQUÉ : {} (v{})", game.getTitle(), newVersion);
        }
    }

    // --- ACHAT (Méthode appelée par le Consumer) ---
    public void processSale(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jeu introuvable pour mise à jour stats"));

        game.setSalesCount(game.getSalesCount() + 1);
        updateDynamicPrice(game);

        gameRepository.save(game);
        log.info("📈 Ventes mises à jour pour {} : Total {}", game.getTitle(), game.getSalesCount());
    }

    // --- Autres méthodes utilitaires ---

    public void updateRating(String gameId, int stars) {
        gameRepository.findById(gameId).ifPresent(game -> {
            long totalReviews = game.getReviewCount();
            double currentAvg = game.getAverageRating();
            double newAvg = ((currentAvg * totalReviews) + stars) / (totalReviews + 1);

            game.setReviewCount(totalReviews + 1);
            game.setAverageRating(newAvg);
            updateDynamicPrice(game);

            gameRepository.save(game);
        });
    }

    public void updateDynamicPrice(Game game) {
        double multiplier = 1.0;
        if (game.getSalesCount() > 1000) multiplier += 0.10;
        else if (game.getSalesCount() < 50) multiplier -= 0.10;

        if (game.getReviewCount() > 5) {
            if (game.getAverageRating() >= 4.5) multiplier += 0.10;
            else if (game.getAverageRating() < 3.0) multiplier -= 0.20;
        }

        double newPrice = game.getBasePrice() * multiplier;
        if (newPrice < game.getBasePrice() * 0.5) newPrice = game.getBasePrice() * 0.5;
        if (newPrice > game.getBasePrice() * 1.5) newPrice = game.getBasePrice() * 1.5;

        game.setCurrentPrice(Math.round(newPrice * 100.0) / 100.0);
    }

    private boolean isVersionNewer(String current, String target) {
        if (current == null) return true;
        return target.compareTo(current) > 0;
    }

    public List<Game> getAllGames() { return gameRepository.findAll(); }
    public Optional<Game> getGameById(String id) { return gameRepository.findById(id); }
    public List<Game> getGamesByPlatform(String platform) { return gameRepository.findByPlatform(platform); }
    public List<Game> getGamesByPublisher(String publisherName) { return gameRepository.findByPublisherName(publisherName); }
}