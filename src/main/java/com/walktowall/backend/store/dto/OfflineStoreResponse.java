package com.walktowall.backend.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OfflineStoreResponse {

    private Integer storeId;
    private Integer regionCategory;
    private String storeName;
}
