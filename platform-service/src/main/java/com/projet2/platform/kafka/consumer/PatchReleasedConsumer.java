package com.projet2.platform.kafka.consumer;

import com.projet2.events.PatchReleased;
import com.projet2.events.PatchChange;
import com.projet2.platform.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PatchReleasedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatchReleasedConsumer.class);

    private final GameService gameService;

    public PatchReleasedConsumer(GameService gameService) {
        this.gameService = gameService;
    }

    @KafkaListener(topics = "patch-released", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PatchReleased event) {
        log.info("-------------------------------------------------------------");
        log.info("📥 KAFKA : Patch reçu pour '{}' (ID: {})", event.getGameName(), event.getGameId());

        gameService.applyPatch(
                event.getGameId().toString(),
                event.getPlatform().toString(),
                event.getNewVersion().toString(),
                event.getEditorComment().toString()
        );

        if (event.getChanges() != null && !event.getChanges().isEmpty()) {
            log.info("   - Détail des changements :");
            for (PatchChange change : event.getChanges()) {
                log.info("     * [{}] {}", change.getType(), change.getDescription());
            }
        }
        log.info("-------------------------------------------------------------");
    }
}