package com.walktowall.backend.product.repository;

import com.walktowall.backend.product.entity.ProductScanEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductScanRepository extends JpaRepository<ProductScanEntity, Integer> {
    @EntityGraph(attributePaths = {"product"})
    List<ProductScanEntity> findAllByVisitCard_VisitCardId(Integer visitCardId);
}