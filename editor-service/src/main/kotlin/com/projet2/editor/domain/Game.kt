package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "games", indexes = [
    Index(name = "idx_game_id", columnList = "game_id"),
    Index(name = "idx_publisher", columnList = "publisher_id")
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

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    fun applyPatch(newVersion: String, description: String, changes: List<String>): Patch {
        require(newVersion > versionInfo.currentVersion) {
            "New version $newVersion must be greater than current ${versionInfo.currentVersion}"
        }

        return Patch(
            game = this,
            previousVersion = versionInfo.currentVersion,
            newVersion = newVersion,
            description = description,
            changes = changes
        )
    }

    fun updateVersion(newVersion: String): Game {
        return copy(versionInfo = versionInfo.copy(currentVersion = newVersion))
    }
}

@Embeddable
data class VersionInfo(
    @Column(name = "current_version", nullable = false)
    val currentVersion: String = "1.0.0",

    @Column(name = "is_early_access")
    val isEarlyAccess: Boolean = true
) {
    fun isBeta() = currentVersion.startsWith("0.")
}