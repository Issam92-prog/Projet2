package com.projet2.platform.repository;

import com.projet2.platform.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    Optional<Game> findByTitle(String title);

    @Query("SELECT g FROM Game g JOIN g.versions v WHERE KEY(v) = :platform")
    List<Game> findByPlatform(@Param("platform") String platform);
    // -------------------------------

    List<Game> findByPublisherName(String publisherName);

    List<Game> findByIsEarlyAccess(Boolean isEarlyAccess);
}