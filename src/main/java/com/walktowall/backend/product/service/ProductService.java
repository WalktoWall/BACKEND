package com.walktowall.backend.product.service;

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
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VisitCardRepository visitCardRepository;
    private final ProductScanRepository productScanRepository;
    private final BestProductRepository bestProductRepository;

    // 유사도 계산 객체
    private final JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();

    public ProductDetailResponse getProductDetail(Integer productId) {
        if (productId == null || productId <= 0)
            throw new IllegalArgumentException("productId는 1 이상의 정수 형태여야 합니다.");

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));

        return ProductDetailResponse.builder()
                .message("상품 상세 조회에 성공했습니다.")
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productImg(product.getProductImg())
                .build();
    }

    @Transactional
    public RecordProductScanResponse recordProductScan(Integer userId, String productName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방문 카드를 찾을 수 없습니다."));

        ProductEntity product = findMostSimilarProduct(productName)
                .orElseThrow(() -> new IllegalArgumentException("인식된 텍스트와 일치하는 상품을 찾을 수 없습니다."));

        ProductScanEntity productScan = ProductScanEntity.builder()
                .product(product)
                .visitCard(visitCard)
                .build();

        productScanRepository.save(productScan);

        return RecordProductScanResponse.builder()
                .message("스캔 상품이 등록되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public ProductHistoryResponse getProductHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방문 카드를 찾을 수 없습니다."));

        List<ProductScanEntity> productScanList = productScanRepository.findAllByVisitCard_VisitCardId(visitCard.getVisitCardId());

        List<ProductHistoryResponse.Product> productList = new ArrayList<>();
        for (ProductScanEntity p : productScanList) {
            ProductHistoryResponse.Product product = ProductHistoryResponse.Product.builder()
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
    public ReadBestProductResponse readBestProducts() {
        List<BestProductEntity> bestProductEntityList = bestProductRepository.findAll();
        List<ReadBestProductResponse.BestProduct> bestProductList = new ArrayList<>();

        for (BestProductEntity bP : bestProductEntityList) {
            ReadBestProductResponse.BestProduct bestProduct = new ReadBestProductResponse.BestProduct(
                    bP.getProduct().getProductId(), bP.getProduct().getProductName());
            bestProductList.add(bestProduct);
        }

        return ReadBestProductResponse.builder()
                .message("베스트 상품 조회에 성공했습니다.")
                .bestProductList(bestProductList)
                .build();
    }

    // OCR로 들어온 productName과 가장 유사한 ProductEntity를 탐색
    private Optional<ProductEntity> findMostSimilarProduct(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return Optional.empty();
        }

        String cleanedInput = cleanText(rawName);

        // 1. DB 원본 완전히 일치하는 경우 (최우선)
        Optional<ProductEntity> exactMatch = productRepository.findByProductName(rawName.trim());
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        List<ProductEntity> candidates = productRepository.findAll();

        // 2. 입력 텍스트가 DB 상품명에 포함되거나, DB 상품명이 입력 텍스트에 포함되는 경우 (2순위)
        // 예: "Aren 비세토스 E/W 숄더" <-> "Aren 비세토스 E/W 숄더백"
        for (ProductEntity product : candidates) {
            String cleanedDbName = cleanText(product.getProductName());
            if (!cleanedInput.isEmpty() && !cleanedDbName.isEmpty()) {
                if (cleanedDbName.contains(cleanedInput) || cleanedInput.contains(cleanedDbName)) {
                    return Optional.of(product);
                }
            }
        }

        // 3. 포함 관계가 없을 때만 유사도(Jaro-Winkler) 비교 진행 (3순위)
        ProductEntity bestMatch = null;
        double maxSimilarity = -1.0;

        for (ProductEntity product : candidates) {
            String cleanedDbName = cleanText(product.getProductName());
            double similarity = jaroWinkler.apply(cleanedInput, cleanedDbName);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = product;
            }
        }

        // 임계값을 0.75~0.8 정도로 높여 엉뚱한 상품 매칭 방지
        double THRESHOLD = 0.75;
        if (maxSimilarity >= THRESHOLD) {
            return Optional.ofNullable(bestMatch);
        }

        return Optional.empty();
    }

    // 알파벳, 한글, 숫자만 남기고 제거하는 전처리 메서드
    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-Z0-9가-힣]", "").toUpperCase();
    }
}