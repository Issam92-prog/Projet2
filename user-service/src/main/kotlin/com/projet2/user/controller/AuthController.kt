package com.projet2.user.controller

import com.projet2.user.dto.CreateUserRequest
import com.projet2.user.model.User
import com.projet2.user.repository.UserRepo
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepo: UserRepo,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/register")
    fun register(@RequestBody request: CreateUserRequest): ResponseEntity<Any> {

        // 1. Vérifications d'unicité
        if (userRepo.findByPseudo(request.pseudo).isPresent) {
            return ResponseEntity.badRequest().body("⛔ Erreur: Le pseudo '${request.pseudo}' est déjà pris.")
        }
        if (userRepo.findByEmail(request.email).isPresent) {
            return ResponseEntity.badRequest().body("⛔ Erreur: L'email '${request.email}' est déjà utilisé.")
        }

        // 2. Création de l'entité User avec TES champs
        val newUser = User(
            pseudo = request.pseudo,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            birthDate = request.birthDate,
            // Hachage du mot de passe
            password = passwordEncoder.encode(request.password)
        )

        // 3. Sauvegarde
        val savedUser = userRepo.save(newUser)

        return ResponseEntity.ok(mapOf(
            "message" to "Utilisateur créé avec succès !",
            "userId" to savedUser.id,
            "pseudo" to savedUser.pseudo
        ))
    }
}