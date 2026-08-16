package com.walktowall.backend.staff.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffHomeSummaryResponse {
    private long todayVisitCount;   // 오늘 방문 예정 총 인원
    private long storeArrivalCount; // 매장 도착 인원
    private long completedCount;    // 응대 완료 인원
}