package com.projet2.platform.service;

import com.projet2.platform.entity.Game;
import com.projet2.platform.kafka.producer.GameCrashProducer;
import com.projet2.platform.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service de monitoring de l'exécution des jeux.
 * Simule la détection de crashs et génère des rapports d'incidents.
 */
@Service
public class GameMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(GameMonitoringService.class);

    private final GameCrashProducer crashReportProducer;
    private final GameRepository gameRepository;
    private final Random random = new Random();

    // Messages d'erreur réalistes pour différents types de crashs
    private static final String[] ERROR_MESSAGES = {
            "NullPointerException: Cannot invoke method on null object",
            "OutOfMemoryError: Java heap space exceeded",
            "StackOverflowError: Recursive function call limit reached",
            "IndexOutOfBoundsException: Array index -1 out of bounds",
            "ClassNotFoundException: Unable to load game asset",
            "ConcurrentModificationException: Collection modified during iteration",
            "IllegalStateException: Game in invalid state for this operation",
            "NetworkException: Connection timeout after 30000ms",
            "GraphicsException: Failed to initialize DirectX renderer",
            "AudioException: Sound buffer overflow detected",
            "FileNotFoundException: Save game file not found",
            "Fatal Error: GPU memory allocation failed"
    };

    private static final String[] STACK_TRACES = {
            "at com.game.engine.PhysicsEngine.update(PhysicsEngine.java:342)\n" +
                    "at com.game.core.GameLoop.tick(GameLoop.java:156)\n" +
                    "at com.game.core.Application.run(Application.java:89)",

            "at com.game.renderer.TextureManager.loadTexture(TextureManager.java:217)\n" +
                    "at com.game.world.TerrainGenerator.generate(TerrainGenerator.java:103)\n" +
                    "at com.game.world.WorldLoader.initializeWorld(WorldLoader.java:45)",

            "at com.game.ai.PathfindingEngine.calculatePath(PathfindingEngine.java:278)\n" +
                    "at com.game.entities.NPC.updateAI(NPC.java:134)\n" +
                    "at com.game.world.EntityManager.updateAll(EntityManager.java:67)",

            "at com.game.network.MultiplayerSession.sync(MultiplayerSession.java:512)\n" +
                    "at com.game.network.NetworkManager.update(NetworkManager.java:89)\n" +
                    "at com.game.core.GameLoop.networkTick(GameLoop.java:201)"
    };

    public GameMonitoringService(GameCrashProducer crashReportProducer, GameRepository gameRepository) {
        this.crashReportProducer = crashReportProducer;
        this.gameRepository = gameRepository;
    }

    /**
     * Simule un crash pour un jeu donné
     * Dans un système réel, cette méthode serait appelée par un système de télémétrie
     * qui détecte les crashs des clients
     */
    public void simulateCrash(String gameId) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (gameOpt.isEmpty()) {
            log.warn("⚠️ Impossible de simuler un crash : jeu {} non trouvé", gameId);
            return;
        }

        Game game = gameOpt.get();

        // Sélectionne aléatoirement une plateforme parmi celles disponibles
        if (game.getVersions().isEmpty()) {
            log.warn("⚠️ Impossible de simuler un crash : aucune plateforme pour {}", game.getTitle());
            return;
        }

        String[] platforms = game.getVersions().keySet().toArray(new String[0]);
        String randomPlatform = platforms[random.nextInt(platforms.length)];

        // Génère un crash aléatoire
        String errorMessage = ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
        String stackTrace = STACK_TRACES[random.nextInt(STACK_TRACES.length)];

        log.info("🎮 Simulation crash pour '{}' sur {}", game.getTitle(), randomPlatform);

        crashReportProducer.reportCrash(
                game.getId(),
                game.getTitle(),
                randomPlatform,
                errorMessage,
                stackTrace
        );
    }

    /**
     * Simule des crashs aléatoires pour plusieurs jeux
     * Utile pour tester le système de monitoring
     */
    public void simulateRandomCrashes(int numberOfCrashes) {
        log.info("🔥 Simulation de {} crashs aléatoires...", numberOfCrashes);

        for (int i = 0; i < numberOfCrashes; i++) {
            // Récupère un jeu aléatoire du catalogue
            long gameCount = gameRepository.count();
            if (gameCount == 0) {
                log.warn("⚠️ Aucun jeu dans le catalogue pour simuler des crashs");
                return;
            }

            // Sélectionne un jeu aléatoire
            long randomIndex = ThreadLocalRandom.current().nextLong(gameCount);
            Game randomGame = gameRepository.findAll().get((int) randomIndex);

            simulateCrash(randomGame.getId());

            // Petit délai entre chaque crash pour simuler un comportement réaliste
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interruption lors de la simulation des crashs");
                break;
            }
        }

        log.info("✅ Simulation terminée : {} crashs générés", numberOfCrashes);
    }

    /**
     * Simule un crash spécifique avec des paramètres personnalisés
     */
    public void reportCustomCrash(String gameId, String platform, String errorMessage, String stackTrace) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (gameOpt.isEmpty()) {
            log.warn("⚠️ Jeu {} non trouvé, impossible d'envoyer le rapport", gameId);
            return;
        }

        Game game = gameOpt.get();

        // Vérifie que le jeu est disponible sur cette plateforme
        if (!game.getVersions().containsKey(platform)) {
            log.warn("⚠️ Le jeu '{}' n'est pas disponible sur {}", game.getTitle(), platform);
            return;
        }

        crashReportProducer.reportCrash(gameId, game.getTitle(), platform, errorMessage, stackTrace);
    }
}