package com.walktowall.backend.visitcard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface VisitCardRepository extends JpaRepository<VisitCard, Integer> {
    Optional<VisitCard> findFirstByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    List<VisitCard> findByVisitTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}