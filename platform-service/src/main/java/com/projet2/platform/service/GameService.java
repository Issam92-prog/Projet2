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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameRepository gameRepository;

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
            game.setCreatedAt(Instant.ofEpochMilli(event.getPublishedAt()));
            log.info("   ✨ Création d'un nouveau jeu");
        }

        game.setTitle(event.getGameName().toString());
        game.setPlatform(event.getPlatform().toString());
        game.setPublisherName(event.getPublisherName().toString());
        game.setCurrentVersion(event.getVersion().toString());
        game.setPublisherId(null);
        game.setIsEarlyAccess(false);

        List<String> genres = event.getGenre().stream()
                .map(CharSequence::toString)
                .collect(Collectors.toList());
        game.setGenres(genres);

        Game savedGame = gameRepository.save(game);
        log.info("   ✅ Jeu sauvegardé : {} (ID: {})", savedGame.getTitle(), savedGame.getId());

        return savedGame;
    }

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