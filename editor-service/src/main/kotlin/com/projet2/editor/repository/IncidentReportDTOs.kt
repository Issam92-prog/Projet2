package com.projet2.editor.repository

/**
 * DTO pour les statistiques d'incidents par jeu
 */
interface GameIncidentStats {
    val gameId: String
    val count: Long
}

/**
 * DTO pour les statistiques d'incidents par plateforme
 */
interface PlatformIncidentCount {
    val platform: String
    val count: Long
}