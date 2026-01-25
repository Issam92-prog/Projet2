package com.projet2.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un jeu vidéo dans le catalogue de la plateforme
 * Correspond à la table 'video_games' et à l'événement Avro 'GamePublished'
 */
@Entity
@Table(name = "video_games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "publisher_name", nullable = false)
    private String publisherName;

    @Column(name = "publisher_id")
    private String publisherId;

    @Column(name = "current_version", nullable = false)
    private String currentVersion;

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