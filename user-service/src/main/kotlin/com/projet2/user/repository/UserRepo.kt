package com.projet2.user.repository

import com.projet2.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepo : JpaRepository<User, Long> {
    // On cherche par pseudo maintenant
    fun findByPseudo(pseudo: String): Optional<User>

    // Utile pour vérifier si l'email est déjà pris
    fun findByEmail(email: String): Optional<User>
}