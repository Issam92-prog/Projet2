package com.projet2.user.controller

import com.projet2.user.dto.LoginRequest
import com.projet2.user.dto.RegisterRequest
import com.projet2.user.repository.UserRepo
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepo: UserRepo,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): String {

        if (userRepo.existsByEmail(req.email)) {
            return "Email already used"
        }

        val user = com.projet2.user.model.User(
            pseudo = req.pseudo,
            firstName = req.firstName,
            lastName = req.lastName,
            birthDate = req.birthDate,
            email = req.email,
            password = passwordEncoder.encode(req.password)
                ?: throw IllegalStateException("Password encoding failed")

        )

        userRepo.save(user)
        return "User registered"
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): String {

        val user = userRepo.findByEmail(req.email)
            ?: return "Invalid credentials"

        if (!passwordEncoder.matches(req.password, user.password)) {
            return "Invalid credentials"
        }

        return "Login successful"
    }
}
