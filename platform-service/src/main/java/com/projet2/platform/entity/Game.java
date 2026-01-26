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

    @Column(name = "publisher_name", nullable = false)
    private String publisherName;

    // --- PRIX et DEMANDE et QUALITÉ ---
    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "current_price")
    private Double currentPrice;

    @Column(name = "sales_count")
    @Builder.Default
    private Long salesCount = 0L;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Long reviewCount = 0L;
    // -------------------------------

    // --- DLC ---
    @Column(name = "is_dlc")
    @Builder.Default
    private Boolean isDlc = false;

    // INDISPENSABLE : C'est le seul lien vers le jeu "père"
    @Column(name = "parent_game_id")
    private String parentGameId;

    // --- VERSIONS PAR PLATEFORME ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "video_game_versions",
            joinColumns = @JoinColumn(name = "video_game_id")
    )
    @MapKeyColumn(name = "platform")
    @Column(name = "version")
    @Builder.Default
    private Map<String, String> versions = new HashMap<>();

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