package com.projet2.platform;

import com.projet2.platform.entity.GameEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private final GameRepository gameRepository;


    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    public void applyPatch(String gameId, String platform, String newVersion, String comment) {

        // 1 Chercher le jeu en base
        Optional<GameEntity> gameOpt = gameRepository.findById(gameId);

        if (gameOpt.isEmpty()) {
            log.warn("Jeu inconnu au catalogue (ID: {}).", gameId);
            return;
        }

        GameEntity game = gameOpt.get();

        // 2 Vérifier si on gère cette plateforme pour ce jeu
        if (!game.getVersions().containsKey(platform)) {
            log.warn("⛔ IGNORE : Le jeu '{}' n'est pas vendu sur {} chez nous.", game.getName(), platform);
            return;
        }

        String currentVersion = game.getVersions().get(platform);

        // 3 Vérifier l'antériorité
        if (isVersionNewer(currentVersion, newVersion)) {
            log.info("✅ MISE À JOUR : {} sur {} passe de {} à {}.", game.getName(), platform, currentVersion, newVersion);

            // Mise à jour de la map
            game.getVersions().put(platform, newVersion);

            // Sauvegarde en base
            gameRepository.save(game);

            log.info("  Note éditeur enregistrée : {}", comment);
        } else {
            log.warn("⚠️ OBSOLÈTE : Version reçue ({}) <= Version actuelle ({}).", newVersion, currentVersion);
        }
    }

    private boolean isVersionNewer(String current, String target) {
        // Comparaison simple de chaines
        return target.compareTo(current) > 0;
    }
}