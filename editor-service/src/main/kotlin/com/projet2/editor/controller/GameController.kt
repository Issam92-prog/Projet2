package com.projet2.editor.controller

import com.projet2.editor.dto.GameDTO
import com.projet2.editor.repository.GameRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameRepository: GameRepository
) {

    @GetMapping
    fun getAllGames(
        @RequestParam(required = false) platform: String?
    ): List<GameDTO> {
        val games = if (platform != null) {
            gameRepository.findByPlatform(platform)
        } else {
            gameRepository.findAll()
        }

        return games.map {
            GameDTO(
                id = it.id,
                gameId = it.gameId,
                name = it.name,
                platform = it.platform,
                genre = it.genre,
                currentVersion = it.versionInfo.currentVersion,
                publisherName = it.publisher.name
            )
        }
    }

    @GetMapping("/{gameId}")
    fun getGame(@PathVariable gameId: String): GameDTO? {
        return gameRepository.findByGameIdWithPublisher(gameId)
            .map {
                GameDTO(
                    id = it.id,
                    gameId = it.gameId,
                    name = it.name,
                    platform = it.platform,
                    genre = it.genre,
                    currentVersion = it.versionInfo.currentVersion,
                    publisherName = it.publisher.name
                )
            }
            .orElse(null)
    }
}