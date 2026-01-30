package com.projet2.platform.kafka.consumer;

import com.projet2.events.PatchReleased;
import com.projet2.events.PatchChange;
import com.projet2.platform.service.GameService;
import com.projet2.platform.entity.PatchHistory; // Assurez-vous d'avoir créé cette classe
import com.projet2.platform.repository.PatchHistoryRepository; // Et ce repo
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Pour l'injection
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PatchReleasedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatchReleasedConsumer.class);

    private final GameService gameService;

    // On ajoute le Repository pour l'historique
    private final PatchHistoryRepository patchHistoryRepository;

    public PatchReleasedConsumer(GameService gameService, PatchHistoryRepository patchHistoryRepository) {
        this.gameService = gameService;
        this.patchHistoryRepository = patchHistoryRepository;
    }

    @KafkaListener(topics = "patch-released", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PatchReleased event) {
        log.info("-------------------------------------------------------------");
        log.info("📥 KAFKA : Patch reçu pour '{}' (ID: {})", event.getGameName(), event.getGameId());

        // 1. Mise à jour du jeu (Logique métier)
        // Notez l'utilisation de .getNewVersion()
        gameService.applyPatch(
                event.getGameId().toString(),
                event.getPlatform().toString(),
                event.getNewVersion().toString(),
                event.getEditorComment().toString()
        );

        // 2. Archivage dans l'historique (Nouveau code corrigé)
        try {
            PatchHistory history = new PatchHistory();
            history.setGameId(event.getGameId().toString());

            // ✅ CORRECTION ICI : .getNewVersion() au lieu de .getVersion()
            history.setVersion(event.getNewVersion().toString());

            // ✅ CORRECTION ICI : .getEditorComment() au lieu de .getPatchNotes()
            history.setDescription(event.getEditorComment().toString());

            history.setReleaseDate(Instant.ofEpochMilli(event.getReleasedAt()));

            patchHistoryRepository.save(history);
            log.info("📜 Historique sauvegardé : v{}", event.getNewVersion());

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'archivage du patch : {}", e.getMessage());
        }

        // 3. Logs détaillés (Optionnel)
        if (event.getChanges() != null && !event.getChanges().isEmpty()) {
            log.info("   - Détail des changements :");
            for (PatchChange change : event.getChanges()) {
                log.info("     * [{}] {}", change.getType(), change.getDescription());
            }
        }
        log.info("-------------------------------------------------------------");
    }
}