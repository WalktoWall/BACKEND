package com.walktowall.backend.store.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalTime;

@Getter
@Builder
public class OfflineStoreResponse {

    private Integer storeId;
    private Integer regionCategory;
    private String storeName;
    private LocalTime openTime;
    private LocalTime closeTime;
}
