package com.projet2.editor.repository

import com.projet2.editor.domain.Publisher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PublisherRepository : JpaRepository<Publisher, Long> {

    /**
     * Recherche un éditeur par son nom
     */
    fun findByName(name: String): Optional<Publisher>

    /**
     * Recherche un éditeur par nom avec ses métadonnées (évite N+1 queries)
     */
    @Query("SELECT p FROM Publisher p LEFT JOIN FETCH p.metadata WHERE p.name = :name")
    fun findByNameWithMetadata(@Param("name") name: String): Optional<Publisher>

    /**
     * Vérifie si un éditeur existe par son nom
     */
    fun existsByName(name: String): Boolean

    /**
     * Compte le nombre d'éditeurs
     */
    override fun count(): Long
}