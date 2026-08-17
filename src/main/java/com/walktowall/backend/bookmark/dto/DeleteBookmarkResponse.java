package com.walktowall.backend.bookmark.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteBookmarkResponse {
    private String message;
}
