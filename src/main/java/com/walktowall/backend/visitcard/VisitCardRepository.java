package com.walktowall.backend.visitcard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitCardRepository extends JpaRepository<VisitCard, Integer> {
    Optional<VisitCard> findByUser_UserId(Integer userId);
}