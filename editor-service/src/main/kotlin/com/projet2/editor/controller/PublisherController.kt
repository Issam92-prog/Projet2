package com.projet2.editor.controller

import com.projet2.editor.dto.PublisherDTO
import com.projet2.editor.repository.PublisherRepository
import com.projet2.editor.repository.GameRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/publishers")
class PublisherController(
    private val publisherRepository: PublisherRepository,
    private val gameRepository: GameRepository
) {

    @GetMapping
    fun getAllPublishers(): List<PublisherDTO> {
        return publisherRepository.findAll().map {
            PublisherDTO(
                id = it.id,
                name = it.name,
                totalGamesPublished = it.metadata.totalGamesPublished,
                createdAt = it.metadata.createdAt.toString()
            )
        }
    }

    @GetMapping("/{id}")
    fun getPublisher(@PathVariable id: Long): ResponseEntity<PublisherDTO> {
        return publisherRepository.findById(id)
            .map {
                ResponseEntity.ok(PublisherDTO(
                    id = it.id,
                    name = it.name,
                    totalGamesPublished = it.metadata.totalGamesPublished,
                    createdAt = it.metadata.createdAt.toString()
                ))
            }
            .orElse(ResponseEntity.notFound().build())
    }
}