package com.projet2.editor.repository

import com.projet2.editor.domain.Game
import com.projet2.editor.domain.Publisher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GameRepository : JpaRepository<Game, Long> {

    /**
     * Recherche un jeu par son gameId (UUID)
     */
    fun findByGameId(gameId: String): Optional<Game>

    /**
     * Recherche un jeu avec son éditeur (JOIN FETCH pour éviter lazy loading)
     */
    @Query("SELECT g FROM Game g JOIN FETCH g.publisher WHERE g.gameId = :gameId")
    fun findByGameIdWithPublisher(@Param("gameId") gameId: String): Optional<Game>

    /**
     * Récupère tous les jeux d'un éditeur
     */
    fun findByPublisher(publisher: Publisher): List<Game>

    /**
     * Récupère tous les jeux d'une plateforme
     */
    fun findByPlatform(platform: String): List<Game>

    /**
     * Récupère tous les jeux d'un genre
     */
    fun findByGenre(genre: String): List<Game>

    /**
     * Récupère les jeux en early access (version < 1.0)
     */
    @Query("SELECT g FROM Game g WHERE g.versionInfo.isEarlyAccess = true")
    fun findEarlyAccessGames(): List<Game>

    /**
     * Récupère les jeux en version beta (version commençant par 0.)
     */
    @Query("SELECT g FROM Game g WHERE g.versionInfo.currentVersion LIKE '0.%'")
    fun findBetaGames(): List<Game>

    /**
     * Recherche des jeux par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(@Param("name") name: String): List<Game>

    /**
     * Compte le nombre de jeux par éditeur
     */
    fun countByPublisher(publisher: Publisher): Long

    /**
     * Vérifie si un jeu existe par gameId
     */
    fun existsByGameId(gameId: String): Boolean
}