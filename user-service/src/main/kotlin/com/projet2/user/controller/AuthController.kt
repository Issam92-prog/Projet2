package com.projet2.user.controller

import com.projet2.user.dto.CreateUserRequest
import com.projet2.user.model.User
import com.projet2.user.repository.UserRepo
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepo: UserRepo,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(@RequestBody request: CreateUserRequest): ResponseEntity<Any> {

        // 1. Vérifications d'unicité
        if (userRepo.findByPseudo(request.pseudo).isPresent) {
            log.warn("⚠️ Tentative d'inscription échouée : Pseudo '${request.pseudo}' déjà pris.")
            return ResponseEntity.badRequest().body("⛔ Erreur: Le pseudo '${request.pseudo}' est déjà pris.")
        }
        if (userRepo.findByEmail(request.email).isPresent) {
            log.warn("⚠️ Tentative d'inscription échouée : Email '${request.email}' déjà utilisé.")
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

        log.info("👤 NOUVEL UTILISATEUR : {} (ID: {}) vient de s'inscrire.", savedUser.pseudo, savedUser.id)

        return ResponseEntity.ok(mapOf(
            "message" to "Utilisateur créé avec succès !",
            "userId" to savedUser.id,
            "pseudo" to savedUser.pseudo
        ))
    }
}