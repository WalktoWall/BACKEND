package com.walktowall.backend.store;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfflineStoreRepository extends JpaRepository<OfflineStore, Integer> {
    List<OfflineStore> findByRegionCategory(Integer regionCategory);
}