package com.walktowall.backend.product.service;

import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.dto.ProductHistoryResponse;
import com.walktowall.backend.product.dto.RecordProductScanResponse;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.entity.ProductScanEntity;
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

    // 유사도 계산 객체
    private final JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();

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
                .productName(product.getProductName())
                .productImg(product.getProductImg())
                .build();
    }

    @Transactional
    public RecordProductScanResponse recordProductScan(Integer userId, String productName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        VisitCard visitCard = visitCardRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("해당 방문 카드를 찾을 수 없습니다.");
                });

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

     // OCR로 들어온 productName과 가장 유사한 ProductEntity를 탐색
    private Optional<ProductEntity> findMostSimilarProduct(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return Optional.empty();
        }

        // 1. OCR 텍스트 전처리 (공백 및 특수문자 제거, 대문자 변환)
        String cleanedInput = cleanText(rawName);

        // 2. DB에서 원본 이름으로 완전 일치하는 상품 먼저 탐색 (성능 최적화)
        Optional<ProductEntity> exactMatch = productRepository.findByProductName(rawName.trim());
        if (exactMatch.isPresent()) {
            return exactMatch;
        }
        // 3. 후보군 추출: 전체 상품을 가져와 메모리에서 비교
        // (상품 수가 매우 많은 경우 특정 카테고리나 키워드로 후보군을 1차 필터링하는 것이 좋습니다)
        List<ProductEntity> candidates = productRepository.findAll();

        ProductEntity bestMatch = null;
        double maxSimilarity = -1.0;

        // 4. Jaro-Winkler 알고리즘으로 가장 높은 유사도를 가진 상품 탐색
        for (ProductEntity product : candidates) {
            String cleanedDbName = cleanText(product.getProductName());

            double similarity = jaroWinkler.apply(cleanedInput, cleanedDbName);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = product;
            }
        }

        // 5. 유사도 임계값(Threshold) 검증 (0.6 미만이면 엉뚱한 상품 매칭 방지)
        double THRESHOLD = 0.60;
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