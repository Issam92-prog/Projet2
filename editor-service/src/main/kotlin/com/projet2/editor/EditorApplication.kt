package com.projet2.editor

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class EditorApplication {
    @Bean
    fun init(gamePublisherService: GamePublisherService) = CommandLineRunner {
        println("Lancement de l'importation CSV...")
        gamePublisherService.publishGamesFromCsv()
        println("Importation terminée !")
    }
}

fun main(args: Array<String>) {
    runApplication<EditorApplication>(*args)
}