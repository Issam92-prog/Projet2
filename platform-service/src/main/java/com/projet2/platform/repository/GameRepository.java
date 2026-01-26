package com.projet2.platform.repository;

import com.projet2.platform.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

    Optional<Game> findByTitle(String title);

    List<Game> findByPlatform(String platform);

    List<Game> findByPublisherName(String publisherName);

    List<Game> findByIsEarlyAccess(Boolean isEarlyAccess);
}