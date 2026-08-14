package com.walktowall.backend.visitcard.controller;

import com.walktowall.backend.visitcard.dto.RouteProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")

public class RecommendController {
    private final RecommendService recommendService; // 다음 커밋에서 service 파일 생성 예정

    @GetMapping("/routes/{visitCardId}")
    public ResponseEntity<RouteProductResponse> getRecommendedProductsByVisitCard(
            @PathVariable Integer visitCardId,
            @RequestParam(required = false) String zoneName) {

        RouteProductResponse response = recommendService.getRouteProducts(visitCardId, zoneName);
        return ResponseEntity.ok(response);
    }
}
