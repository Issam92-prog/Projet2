package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "publishers")
data class Publisher(
    @Column(unique = true, nullable = false)
    val name: String,

    @Embedded
    val metadata: PublisherMetadata = PublisherMetadata(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    // Méthodes business domain
    fun publishGame(gameName: String, platform: String, genre: String): Game {
        return Game(
            gameId = java.util.UUID.randomUUID().toString(),
            name = gameName,
            platform = platform,
            genre = genre,
            publisher = this,
            publishedAt = Instant.now()
        )
    }
}

@Embeddable
data class PublisherMetadata(
    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "total_games_published")
    var totalGamesPublished: Int = 0
)