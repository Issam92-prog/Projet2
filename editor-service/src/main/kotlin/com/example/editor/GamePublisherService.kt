package com.example.editor

import com.opencsv.CSVReaderBuilder
import com.projet.events.GamePublished
import org.springframework.core.io.ClassPathResource
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.util.*

@Service
class GamePublisherService(private val kafkaTemplate: KafkaTemplate<String, GamePublished>) {

    fun publishGamesFromCsv() {
        val resource = ClassPathResource("vgsales.csv")
        val reader = InputStreamReader(resource.inputStream)

        CSVReaderBuilder(reader).withSkipLines(1).build().use { csvReader ->
            csvReader.forEach { line ->
                val gameEvent = GamePublished.newBuilder()
                    .setGameId(UUID.randomUUID().toString())
                    .setGameName(line[0])
                    .setPlatform(line[1])
                    .setPublisherName(line[4])
                    .setGenre(listOf(line[3]))
                    .setVersion("1.0.0")
                    .setPublishedAt(System.currentTimeMillis())
                    .build()

                kafkaTemplate.send("game-published", gameEvent.getGameId(), gameEvent)
            }
        }
    }
}