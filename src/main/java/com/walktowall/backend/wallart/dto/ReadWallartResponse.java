package com.walktowall.backend.wallart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadWallartResponse {
    private String message;
    private Integer wallartId;
    private String wallarImg;
    private String wallartText;
}
