package com.walktowall.backend.product.repository;

import com.walktowall.backend.product.entity.ProductScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductScanRepository extends JpaRepository<ProductScanEntity, Integer> {

}