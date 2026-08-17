package com.walktowall.backend.wallart.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendWallartTextResponse {
    private String message;
    private List<String> TextList;
}
