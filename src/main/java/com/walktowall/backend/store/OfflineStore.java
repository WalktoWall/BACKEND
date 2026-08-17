package com.walktowall.backend.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "offline_stores")

public class OfflineStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")   // DB 컬럼명
    private Integer storeId;

    @Column(name = "region_category", nullable = false)
    private Integer regionCategory; // 지역 카테고리 (1=서울, 2=인천 등)

    @Column(name = "store_name", nullable = false)
    private String storeName; // 매장 이름

    // 추가: 영업 시작 시간
    @Column(name = "open_time")
    private LocalTime openTime;

    // 추가: 영업 종료 시간
    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "latitude")
    private Double latitude;  // 위도

    @Column(name = "longitude")
    private Double longitude; // 경도
}
