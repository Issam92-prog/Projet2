package com.projet2.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class PatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameId; // Lien vers le jeu
    private String version;
    private String description;

    private Instant releaseDate;
}