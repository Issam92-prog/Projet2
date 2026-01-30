package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "games", indexes = [
    Index(name = "idx_game_id", columnList = "game_id"),
    Index(name = "idx_publisher", columnList = "publisher_id"),
    Index(name = "idx_is_dlc", columnList = "is_dlc")
])
data class Game(
    @Column(name = "game_id", unique = true, nullable = false)
    val gameId: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val platform: String,

    @Column(nullable = false)
    val genre: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    val publisher: Publisher,

    @Column(name = "published_at")
    val publishedAt: Instant,

    @Embedded
    val versionInfo: VersionInfo = VersionInfo(),

    @Column(name = "is_dlc")
    val isDlc: Boolean = false,

    @Column(name = "base_game_id")
    val baseGameId: String? = null, // Si c'est un DLC, référence au jeu de base

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    /**
     * Met à jour la version du jeu
     */
    fun updateVersion(newVersion: String): Game {
        return this.copy(
            versionInfo = versionInfo.copy(currentVersion = newVersion)
        )
    }

    /**
     * Applique un patch au jeu
     */
    fun applyPatch(newVersion: String, description: String, changes: List<String>): Patch {
        return Patch(
            game = this,
            previousVersion = versionInfo.currentVersion,
            newVersion = newVersion,
            description = description,
            changes = changes
        )
    }

    /**
     * Vérifie si le jeu est en early access
     */
    fun isInEarlyAccess(): Boolean = versionInfo.isEarlyAccess


    /**
     * Vérifie si c'est un jeu de base
     */
    fun isBaseGame(): Boolean = !isDlc
}

/**
 * Informations de version
 */
@Embeddable
data class VersionInfo(
    @Column(name = "current_version")
    val currentVersion: String = "1.0.0",

    @Column(name = "is_early_access")
    val isEarlyAccess: Boolean = false
)