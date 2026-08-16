package com.walktowall.backend.product.repository;

import com.walktowall.backend.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    List<ProductEntity> findAllByZone(String zone);
    Optional<ProductEntity> findByName(String name);

    Optional<ProductEntity> findByProductName(String trim);
}