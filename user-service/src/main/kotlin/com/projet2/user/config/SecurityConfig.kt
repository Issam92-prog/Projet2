package com.projet2.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity // Import essentiel
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity // <--- C'est l'annotation manquante qui active ta config !
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Désactive la protection CSRF pour les APIs
            .authorizeHttpRequests { auth ->
                // On autorise explicitement ton endpoint /auth/**
                auth.requestMatchers("/auth/**").permitAll()
                // On autorise tout le reste pour le développement
                auth.anyRequest().permitAll()
            }
            // IMPORTANT : On désactive l'authentification "Basic" qui te bloque (le 401)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

        return http.build()
    }
}