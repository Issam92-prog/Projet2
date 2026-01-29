package com.projet2.editor.service

import com.projet2.editor.domain.*
import com.projet2.editor.repository.GameRepository
import com.projet2.editor.repository.IncidentReportRepository
import com.projet2.editor.repository.PatchRepository
import com.projet2.events.PatchReleased
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PatchService(
    private val gameRepository: GameRepository,
    private val patchRepository: PatchRepository,
    private val incidentReportRepository: IncidentReportRepository,
    private val kafkaTemplate: KafkaTemplate<String, PatchReleased>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun releasePatch(
        gameId: String,
        newVersion: String,
        description: String,
        changes: List<String>,
        type: PatchType = PatchType.BUGFIX
    ) {
        // Récupérer le jeu
        val game = gameRepository.findByGameIdWithPublisher(gameId)
            .orElseThrow { IllegalArgumentException("Jeu non trouvé : $gameId") }

        // Créer le patch via la méthode du domaine
        val patchFromDomain = game.applyPatch(newVersion, description, changes)

        // Ajouter le type et sauvegarder
        val patch = patchFromDomain.copy(type = type)
        patchRepository.save(patch)

        // Mettre à jour la version du jeu (immutable)
        val updatedGame = game.updateVersion(newVersion)
        gameRepository.save(updatedGame)

        // Marquer les incidents comme traités
        val processedIncidents = processRelatedIncidents(gameId)

        // Publier l'événement Kafka
        publishPatchEvent(updatedGame, patch)

        log.info("🔧 Patch publié : ${updatedGame.name} v${newVersion} ($processedIncidents incidents traités)")
    }

    private fun processRelatedIncidents(gameId: String): Int {
        val incidents = incidentReportRepository
            .findByGameIdAndStatus(gameId, IncidentStatus.NEW)

        incidents.forEach { incident ->
            incident.markAsProcessed()
        }

        incidentReportRepository.saveAll(incidents)
        return incidents.size
    }

    private fun publishPatchEvent(game: Game, patch: Patch) {
        val patchChanges = patch.changes.map { change ->
            com.projet2.events.PatchChange.newBuilder()
                .setType(patch.type.name)
                .setDescription(change)
                .build()
        }

        val event = PatchReleased.newBuilder()
            .setGameId(game.gameId)
            .setGameName(game.name)
            .setPlatform(game.platform)
            .setNewVersion(patch.newVersion)
            .setEditorComment(patch.description)
            .setChanges(patchChanges)
            .setReleasedAt(patch.releasedAt.toEpochMilli())
            .build()

        kafkaTemplate.send("patch-released", game.gameId, event)
    }
}