package com.walktowall.backend.product.service;

import com.walktowall.backend.bookmark.BookmarkEntity;
import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.dto.ProductHistoryResponse;
import com.walktowall.backend.product.dto.ReadBestProductResponse;
import com.walktowall.backend.product.dto.RecordProductScanResponse;
import com.walktowall.backend.product.entity.BestProductEntity;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.entity.ProductScanEntity;
import com.walktowall.backend.product.repository.BestProductRepository;
import com.walktowall.backend.product.repository.ProductRepository;
import com.walktowall.backend.product.repository.ProductScanRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VisitCardRepository visitCardRepository;
    private final ProductScanRepository productScanRepository;
    private final BestProductRepository bestProductRepository;

    @Transactional
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

    @Transactional
    public RecordProductScanResponse recordProductScan(Integer userId, Integer productId) {
        if (productId == null || productId <= 0)
            throw new IllegalArgumentException("productId는 1 이상의 정수 형태여야 합니다.");

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("해당 상품을 찾을 수 없습니다.");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VisitCard visitCard = visitCardRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("해당 방문 카드를 찾을 수 없습니다.");
                });

        ProductScanEntity productScan = ProductScanEntity.builder()
                .product(product)
                .visitCard(visitCard)
                .build();

        productScanRepository.save(productScan);

        return RecordProductScanResponse.builder()
                .message("스캔 상품이 등록되었습니다.")
                .build();
    }

    @Transactional
    public ProductHistoryResponse getProductHistory(Integer visitCardId) {
        List<ProductScanEntity> productScanList = productScanRepository.findAllByVisitCard_VisitCardId(visitCardId);

        List<ProductHistoryResponse.Product> productList = new ArrayList<>();
        for (ProductScanEntity p : productScanList) {
            ProductHistoryResponse.Product product = ProductHistoryResponse.Product
                    .builder()
                    .productId(p.getProduct().getProductId())
                    .productName(p.getProduct().getProductName())
                    .build();
            productList.add(product);
        }

        return ProductHistoryResponse.builder()
                .message("상품 스캔 히스토리 목록을 성공적으로 불러왔습니다")
                .productList(productList)
                .build();
    }

    @Transactional
    public ReadBestProductResponse readBestProduct() {
        List<BestProductEntity> bestProductEntityList = bestProductRepository.findAll();
        List<ReadBestProductResponse.BestProduct> bestProductList = new ArrayList<>();

        for(BestProductEntity bP : bestProductEntityList) {
            ReadBestProductResponse.BestProduct bestProduct
                    = new ReadBestProductResponse.BestProduct(
                            bP.getProduct().getProductId(), bP.getProduct().getProductName());
            bestProductList.add(bestProduct);
        }

        return ReadBestProductResponse.builder()
                .message("베스트 상품 조회에 성공했습니다.")
                .bestProductList(bestProductList)
                .build();
    }
}