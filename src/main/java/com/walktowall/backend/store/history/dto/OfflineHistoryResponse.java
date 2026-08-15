package com.walktowall.backend.store.history.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OfflineHistoryResponse {

    private Integer historyId;
    private Integer visitCardId;
    private Integer storeId;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;
}