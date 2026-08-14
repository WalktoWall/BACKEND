package com.walktowall.backend.wallart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWallartResponse {
    private String message;
    private Integer wallartId;
}
