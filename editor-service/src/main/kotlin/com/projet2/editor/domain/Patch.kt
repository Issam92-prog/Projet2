package com.projet2.editor.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "patches")
data class Patch(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    val game: Game,

    @Column(name = "previous_version", nullable = false)
    val previousVersion: String,

    @Column(name = "new_version", nullable = false)
    val newVersion: String,

    @Column(columnDefinition = "TEXT")
    val description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "patch_type")
    val type: PatchType = PatchType.BUGFIX,

    @ElementCollection
    @CollectionTable(
        name = "patch_changes",
        joinColumns = [JoinColumn(name = "patch_id")]
    )
    @Column(name = "change_description", length = 500)
    val changes: List<String> = emptyList(),

    @Column(name = "released_at")
    val releasedAt: Instant = Instant.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    init {
        require(changes.isNotEmpty()) { "Patch must have at least one change" }
        require(newVersion > previousVersion) { "New version must be greater than previous" }
    }
}

enum class PatchType {
    BUGFIX,      // correction
    FEATURE,     // ajout
    OPTIMIZATION // optimisation
}