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

    @Transactional
    fun publishGamesFromCsv() {
        // Vérifier si déjà importé
        val existingCount = gameRepository.count()
        if (existingCount > 0) {
            log.info("⏭️ Import CSV ignoré : $existingCount jeux déjà présents en base")
            return
        }

        log.info("📥 Lancement de l'importation CSV...")
        val resource = ClassPathResource("vgsales.csv")
        val reader = InputStreamReader(resource.inputStream)

        CSVReaderBuilder(reader).withSkipLines(1).build().use { csvReader ->
            csvReader.forEachIndexed { index, line ->
                try {
                    val publisherName = line[4]
                    val gameId = UUID.randomUUID().toString()
                    val publishedAt = Instant.now()

                    val publisher = publisherRepository.findByName(publisherName)
                        .orElseGet {
                            val newPublisher = Publisher(
                                name = publisherName,
                                metadata = PublisherMetadata()
                            )
                            publisherRepository.save(newPublisher)
                        }

                    val game = Game(
                        gameId = gameId,
                        name = line[0],
                        platform = line[1],
                        genre = line[3],
                        publisher = publisher,
                        publishedAt = publishedAt,
                        versionInfo = VersionInfo(
                            currentVersion = "1.0.0",
                            isEarlyAccess = false
                        )
                    )
                    gameRepository.save(game)

                    val gameEvent = GamePublished.newBuilder()
                        .setGameId(gameId)
                        .setGameName(line[0])
                        .setPlatform(line[1])
                        .setPublisherName(publisherName)
                        .setGenre(listOf(line[3]))
                        .setVersion("1.0.0")
                        .setPublishedAt(publishedAt.toEpochMilli())
                        .setPrice(59.99)
                        .build()

                    gamePublishedKafkaTemplate.send("game-published", gameEvent.gameId, gameEvent)

                    if ((index + 1) % 100 == 0) {
                        log.info("✅ ${index + 1} jeux publiés...")
                    }

                } catch (e: Exception) {
                    log.error("❌ Erreur ligne $index: ${e.message}")
                }
            }
        }

        val totalGames = gameRepository.count()
        val totalPublishers = publisherRepository.count()
        log.info("🎉 Importation terminée ! $totalGames jeux, $totalPublishers éditeurs")
    }
}