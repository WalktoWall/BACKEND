package com.walktowall.backend.store.history.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreModeResponse {

    private boolean storeMode;
    private Integer storeId;
    private Integer visitCardId;
    private OfflineHistoryResponse history;
}