package com.walktowall.backend.product.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VisitCardRepository visitCardRepository;
    private final ProductScanRepository productScanRepository;
    private final BestProductRepository bestProductRepository;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String openAiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                .productId(productScan.getProduct().getProductId())
                .productName(productScan.getProduct().getProductName())
                .productImg(productScan.getProduct().getProductImg())
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

    /**
     * OpenAI API를 호출하여 입력받은 productName과 가장 유사한 상품을 DB에서 탐색합니다.
     */
    private Optional<ProductEntity> findMostSimilarProduct(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return Optional.empty();
        }

        List<ProductEntity> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) {
            return Optional.empty();
        }

        // DB 전체 상품 목록을 AI 프롬프트용 텍스트 형태로 변환
        String productListPrompt = allProducts.stream()
                .map(p -> String.format("ID: %d | Name: %s", p.getProductId(), p.getProductName()))
                .collect(Collectors.joining("\n"));

        String systemPrompt = "너는 쇼핑몰의 상품 검색 AI 시스템이다. " +
                "사용자가 입력하거나 OCR로 인식한 텍스트와 제공된 상품 목록 중 가장 유사한 상품 1개를 찾아라.\n" +
                "예를 들어 사용자가 입력한 텍스트가 \"OTTOMARCLoOI 다이아몬드 HE 레더 위켄더\"이면 db에서 가장 유사한 상품은 OTTOMAR 다이아몬드 퀼팅 레더 위켄더이다."+
                "유사해보이는 것이라 판단했을 때 바로 반환하지 말고 db의 데이터를 전부 비교해봐라"+
                "응답은 오직 JSON 형태로만 응답해야 하며 다른 설명은 포함하지 마라. 예: {\"productId\": 1}\n" +
                "만약 입력 텍스트와 연관성이 전혀 없는 경우 {\"productId\": null}로 응답해라.";

        String userPrompt = String.format("입력 텍스트: \"%s\"\n\n[상품 목록]\n%s", rawName, productListPrompt);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAiModel);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.0);
            requestBody.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            JsonNode resultJson = objectMapper.readTree(content);
            if (resultJson.has("productId") && !resultJson.get("productId").isNull()) {
                int matchedId = resultJson.get("productId").asInt();
                return productRepository.findById(matchedId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}