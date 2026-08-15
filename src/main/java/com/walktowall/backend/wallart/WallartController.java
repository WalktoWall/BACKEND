package com.walktowall.backend.wallart;

import com.walktowall.backend.wallart.dto.CreateWallartRequest;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wall-art")
public class WallartController {
    private final WallartService wallartService;

    @PostMapping
    ResponseEntity createWallart(@RequestBody CreateWallartRequest request) {
        Integer userId = 1; // 1번 유저로 고정
        CreateWallartResponse response = wallartService.createWallart(request);
        return ResponseEntity.ok(response);
    }
}
