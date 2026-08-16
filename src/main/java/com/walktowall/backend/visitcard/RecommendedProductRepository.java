package com.walktowall.backend.visitcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendedProductRepository extends JpaRepository<RecommendedProduct, Long> {

    @Query("SELECT p FROM RecommendedProduct p WHERE p.visitCard.visitCardId = :visitCardId")
    List<RecommendedProduct> findByVisitCardId(@Param("visitCardId") Integer visitCardId);
}
/*
    // 특정 VisitCard ID로 저장된 추천 상품 목록 조회 (직원용 화면 등에서 사용)
    List<RecommendedProduct> findByVisitCard_VisitCardId(Integer visitCardId);
} */