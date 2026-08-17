package com.walktowall.backend.visitcard.controller;

import com.walktowall.backend.visitcard.dto.RouteProductResponse;
import com.walktowall.backend.visitcard.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.walktowall.backend.visitcard.dto.RecommendProductResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")

public class RecommendController {
    private final RecommendService recommendService;

    @GetMapping("/routes/{visitCardId}")
    public ResponseEntity<RouteProductResponse> getRecommendedProductsByVisitCard(
            @PathVariable Integer visitCardId,
            @RequestParam(required = false) String zoneName) {

        RouteProductResponse response = recommendService.getRouteProducts(visitCardId, zoneName);
        return ResponseEntity.ok(response);
    }

    // 추천 상품 목록 조회
    @GetMapping("/products/{visitCardId}")
    public ResponseEntity<RecommendProductResponse> getRecommendedProducts(
            @PathVariable Integer visitCardId) {

        RecommendProductResponse response =
                recommendService.getRecommendedProducts(visitCardId);

        return ResponseEntity.ok(response);
    }
}
