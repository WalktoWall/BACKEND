package com.walktowall.backend.wallart;

import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.wallart.dto.CreateWallartRequest;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import com.walktowall.backend.wallart.dto.ReadWallartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wall-art")
public class WallartController {
    private final WallartService wallartService;

    @PostMapping
    ResponseEntity createWallart() { // wallart 생성
        Integer userId = 1; // 1번 유저로 고정
        CreateWallartResponse response = wallartService.createWallart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    ResponseEntity readWallart() {
        Integer userId = 1; // 1번 유저로 고정
        ReadWallartResponse response = wallartService.ReadWallart(userId);
        return ResponseEntity.ok(response);
    }
}
