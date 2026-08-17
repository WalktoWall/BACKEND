package com.walktowall.backend.product.controller;

import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.dto.ProductHistoryResponse;
import com.walktowall.backend.product.dto.ReadBestProductResponse;
import com.walktowall.backend.product.dto.RecordProductScanResponse;
import com.walktowall.backend.product.dto.RecordProductScanRequest;
import com.walktowall.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail
            (@PathVariable Integer productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);

        return ResponseEntity.ok(response);
    }

    // 상품 스캔 등록
    @PostMapping("/qr/scans")
    public ResponseEntity<RecordProductScanResponse> addProductScan
        (@RequestBody RecordProductScanRequest request) {
        Integer userId = 1; // 1번 유저로 고정
        RecordProductScanResponse response = productService.recordProductScan(userId, request.getProductName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 상품 스캔 히스토리 조회
    @GetMapping("/qr/history")
    public ResponseEntity<ProductHistoryResponse> getProductHistory() {
        Integer userId = 1; // 1번 유저로 고정
        ProductHistoryResponse response = productService.getProductHistory(userId);
        return ResponseEntity.ok(response);
    }

    // 베스트 상품 조회
    @GetMapping("/best")
    public ResponseEntity getBestProducts() {
        ReadBestProductResponse response = productService.readBestProducts();
        return ResponseEntity.ok(response);
    }
 }
