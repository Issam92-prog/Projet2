package com.projet2.editor.service

import com.opencsv.CSVReaderBuilder
import com.projet2.editor.domain.Game
import com.projet2.editor.domain.Publisher
import com.projet2.editor.domain.PublisherMetadata
import com.projet2.editor.domain.VersionInfo
import com.projet2.editor.repository.GameRepository
import com.projet2.editor.repository.PublisherRepository
import com.projet2.events.GamePublished
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStreamReader
import java.time.Instant
import java.util.UUID

@Service
class GamePublisherService(
    private val gamePublishedKafkaTemplate: KafkaTemplate<String, GamePublished>,
    private val publisherRepository: PublisherRepository,
    private val gameRepository: GameRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun publishGamesFromCsv() {
        if (gameRepository.count() > 0) return // On évite de réimporter si déjà fait

        log.info("📥 Lancement de l'importation CSV avec IDs Uniques...")
        val resource = ClassPathResource("vgsales.csv")
        val reader = InputStreamReader(resource.inputStream)

        CSVReaderBuilder(reader).withSkipLines(1).build().use { csvReader ->
            csvReader.forEachIndexed { index, line ->
                try {
                    val gameName = line[0].trim()
                    val platform = line[1].trim()
                    val genre = line[3].trim()
                    val publisherName = line[4].trim()

                    // ✅ SOLUTION MAGIQUE : ID basé sur le TITRE
                    // "007 Racing" donnera toujours le MEME ID, quelle que soit la plateforme
                    val uniqueId = UUID.nameUUIDFromBytes(gameName.toByteArray()).toString()

                    // 1. Gestion Editeur
                    val publisher = publisherRepository.findByName(publisherName)
                        .orElseGet { publisherRepository.save(Publisher(name = publisherName, metadata = PublisherMetadata())) }

                    // 2. Sauvegarde en Base (Editor)
                    // ✅ CRUCIAL : On ne sauvegarde que si l'ID n'existe pas encore pour éviter le crash Hibernate
                    if (!gameRepository.existsByGameId(uniqueId)) {
                        val game = Game(
                            gameId = uniqueId,
                            name = gameName,
                            platform = "Multi", // On met "Multi" car cet objet représentera toutes les versions coté Editor
                            genre = genre,
                            publisher = publisher,
                            publishedAt = Instant.now(),
                            versionInfo = VersionInfo("1.0.0", false)
                        )
                        gameRepository.save(game)
                    }

                    // 3. Envoi Kafka (Platform Service)
                    // Le Platform Service recevra plusieurs fois le même ID mais avec des plateformes différentes
                    // Il devra faire le "merge" (fusion)
                    val gameEvent = GamePublished.newBuilder()
                        .setGameId(uniqueId)
                        .setGameName(gameName)
                        .setPlatform(platform) // Ici on envoie la VRAIE plateforme (PS1, N64...)
                        .setPublisherName(publisherName)
                        .setGenre(listOf(genre))
                        .setVersion("1.0.0")
                        .setPublishedAt(Instant.now().toEpochMilli())
                        .setPrice(59.99)
                        .build()

                    gamePublishedKafkaTemplate.send("game-published", uniqueId, gameEvent)

                } catch (e: Exception) {
                    log.error("Erreur ligne $index : ${e.message}")
                }
            }
        }
        log.info("🎉 Import terminé : Un seul ID par Jeu !")
    }
}