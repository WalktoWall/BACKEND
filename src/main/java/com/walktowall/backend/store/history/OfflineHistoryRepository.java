package com.walktowall.backend.store.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfflineHistoryRepository
        extends JpaRepository<OfflineHistory, Integer> {

    Optional<OfflineHistory> findByVisitCardVisitCardId(Integer visitCardId);
}