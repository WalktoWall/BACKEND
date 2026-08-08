package com.walktowall.backend.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "offline_stores")

public class OfflineStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")   // DB 컬럼명
    private Long storeId;

    @Column(name = "region_category", nullable = false)
    private Integer regionCategory; // 지역 카테고리 (1=서울, 2=인천 등)

    @Column(name = "store_name", nullable = false)
    private String storeName; // 매장 이름
}
