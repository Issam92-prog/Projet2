package com.projet2.editor.repository

import com.projet2.editor.domain.Game
import com.projet2.editor.domain.Patch
import com.projet2.editor.domain.PatchType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface PatchRepository : JpaRepository<Patch, Long> {

    /**
     * Récupère tous les patchs d'un jeu, triés par date (plus récent en premier)
     */
    fun findByGameOrderByReleasedAtDesc(game: Game): List<Patch>

    /**
     * Récupère tous les patchs d'un type donné
     */
    fun findByType(type: PatchType): List<Patch>

    /**
     * Récupère les patchs d'un jeu par gameId
     */
    @Query("SELECT p FROM Patch p WHERE p.game.gameId = :gameId ORDER BY p.releasedAt DESC")
    fun findByGameIdOrderByReleasedAtDesc(@Param("gameId") gameId: String): List<Patch>

    /**
     * Récupère le dernier patch d'un jeu
     */
    @Query("SELECT p FROM Patch p WHERE p.game.gameId = :gameId ORDER BY p.releasedAt DESC LIMIT 1")
    fun findLatestByGameId(@Param("gameId") gameId: String): Optional<Patch>

    /**
     * Compte le nombre de patchs d'un jeu
     */
    fun countByGame(game: Game): Long

    /**
     * Récupère les patchs publiés après une certaine date
     */
    fun findByReleasedAtAfter(date: Instant): List<Patch>

    /**
     * Récupère les patchs par type et jeu
     */
    fun findByGameAndType(game: Game, type: PatchType): List<Patch>

    /**
     * Compte les patchs de type BUGFIX pour un jeu
     */
    @Query("SELECT COUNT(p) FROM Patch p WHERE p.game = :game AND p.type = 'BUGFIX'")
    fun countBugfixesByGame(@Param("game") game: Game): Long
}