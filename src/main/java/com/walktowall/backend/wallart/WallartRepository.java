package com.walktowall.backend.wallart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WallartRepository extends JpaRepository<WallartEntity, Integer> {
    // visit_card_id(FK)로 WallartEntity 조회
    Optional<WallartEntity> findByVisitCard_VisitCardId(Integer visitCardId);
}