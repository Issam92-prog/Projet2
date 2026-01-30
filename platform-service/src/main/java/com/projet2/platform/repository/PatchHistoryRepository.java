package com.projet2.platform.repository;

import com.projet2.platform.entity.PatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatchHistoryRepository extends JpaRepository<PatchHistory, Long> {
    List<PatchHistory> findByGameIdOrderByReleaseDateDesc(String gameId);
}
