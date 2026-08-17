package com.walktowall.backend.staff.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffCustomerResponse {

    private Integer userId;
    private String userName;

    private Integer visitCardId;

    private LocalDateTime visitTime;

    private String storeName;
}