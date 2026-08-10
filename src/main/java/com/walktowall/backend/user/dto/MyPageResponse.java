package com.walktowall.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponse {
    private String message;
    private Integer userId;
    private String userName;

    @Getter
    @Builder
    private static class styleBoard {
        private Integer storeId;
        private LocalDateTime enterTime;
    }
}
