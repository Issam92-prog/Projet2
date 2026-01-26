package com.projet2.platform;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id //clé primaire
    private String id;

    private String name;

    private String publisherName;

    // Stocke une Map (Clé=Plateforme, Valeur=Version) dans une table jointe
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_versions", joinColumns = @JoinColumn(name = "game_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "version")
    private Map<String, String> versions = new HashMap<>();

    // Constructeurs
    public GameEntity() {}

    public GameEntity(String id, String name, String publisherName) {
        this.id = id;
        this.name = name;
        this.publisherName = publisherName;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public Map<String, String> getVersions() { return versions; }
    public void setVersions(Map<String, String> versions) { this.versions = versions; }
}