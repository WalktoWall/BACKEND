package com.walktowall.backend.product.repository;

import com.walktowall.backend.product.entity.BestProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BestProductRepository extends JpaRepository<BestProductEntity, Integer> {
}
