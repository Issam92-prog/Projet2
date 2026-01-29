package com.projet2.editor.controller

import com.projet2.events.CrashReport
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/test")
class TestController(
    private val crashReportKafka: KafkaTemplate<String, CrashReport>
) {

    @PostMapping("/crash")
    fun simulateCrash(@RequestParam gameId: String): String {
        val crash = CrashReport.newBuilder()
            .setGameId(gameId)
            .setGameName("Test Game")
            .setPlatform("PC")
            .setErrorMessage("NullPointerException in GameEngine.update()")
            .setStackTrace("at com.game.Engine.update(Engine.java:42)")
            .setTimestamp(System.currentTimeMillis())
            .build()

        crashReportKafka.send("crash-report", gameId, crash)
        return "🐛 Crash simulé pour gameId: $gameId"
    }

    @PostMapping("/crashes")
    fun simulateMultipleCrashes(
        @RequestParam gameId: String,
        @RequestParam count: Int = 5
    ): String {
        repeat(count) {
            val crash = CrashReport.newBuilder()
                .setGameId(gameId)
                .setGameName("Test Game")
                .setPlatform("PC")
                .setErrorMessage("Critical error #${it + 1}")
                .setStackTrace("Stack trace #${it + 1}")
                .setTimestamp(System.currentTimeMillis())
                .build()

            crashReportKafka.send("crash-report", gameId, crash)
        }
        return "🐛 $count crashes simulés pour gameId: $gameId"
    }
}