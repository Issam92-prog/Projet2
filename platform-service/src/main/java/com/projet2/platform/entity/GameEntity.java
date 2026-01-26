package com.projet2.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entité représentant un jeu vidéo dans le catalogue.
 * Adaptée pour gérer plusieurs plateformes et versions pour un même jeu.
 */
@Entity
@Table(name = "video_games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    // Renommé 'title' -> 'name' pour être compatible avec ton code actuel
    // (Ou alors tu dois changer .getName() en .getTitle() partout dans ton Service)
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "publisher_name", nullable = false)
    private String publisherName;

    // --- CE QUE NOUS AVONS CHANGÉ ---
    // Au lieu d'avoir 'platform' et 'currentVersion' (1 seule valeur),
    // on garde notre système de Map pour gérer N plateformes.

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_versions", joinColumns = @JoinColumn(name = "game_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "version")
    @Builder.Default // Important avec Lombok pour ne pas écraser l'init
    private Map<String, String> versions = new HashMap<>();

    // --------------------------------

    // On garde les bonus de ton ami (Genres, Early Access, Timestamps)
    @Column(name = "is_early_access")
    @Builder.Default
    private Boolean isEarlyAccess = false;

    @ElementCollection
    @CollectionTable(name = "video_game_genres", joinColumns = @JoinColumn(name = "video_game_id"))
    @Column(name = "genre")
    @Builder.Default
    private List<String> genres = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Gestion automatique des dates
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}