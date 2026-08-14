package com.walktowall.backend.product.service;

import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductDetailResponse getProductDetail(Integer productId) {
        if (productId == null || productId <= 0)
            throw new IllegalArgumentException("productId는 1 이상의 정수 형태여야 합니다.");

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("해당 상품을 찾을 수 없습니다.");
                });

        return ProductDetailResponse.builder()
                .message("상품 상세 조회에 성공했습니다.")
                .productId(product.getProductId())
                .productName(product.getProductDetail())
                .productImg(product.getProductImg())
                .build();
    }
}
