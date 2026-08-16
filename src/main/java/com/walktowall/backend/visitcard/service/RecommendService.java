package com.walktowall.backend.visitcard.service;

import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.repository.ProductRepository;
import com.walktowall.backend.visitcard.RecommendedProductRepository;
import com.walktowall.backend.visitcard.RecommendedProduct;
import com.walktowall.backend.visitcard.dto.RouteProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.walktowall.backend.visitcard.dto.RecommendProductResponse;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendService {

    private final VisitCardRepository visitCardRepository;
    private final ProductRepository productRepository;
    private final RecommendedProductRepository recommendedProductRepository;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";


    /**
     * 추천 동선에 포함된 각 Zone별 상품 추천
     * targetZone이 전달되면 해당 Zone 하나만 조회한다.
     */
    public RouteProductResponse getRouteProducts(
            Integer visitCardId,
            String targetZone
    ) {

        // 1. VisitCard 조회
        VisitCard visitCard = visitCardRepository.findById(visitCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Visit Card입니다."));

        // 2. 이미 결정된 추천 Zone 가져오기
        List<String> zones = new ArrayList<>();

        if (visitCard.getRecommendedRoute() != null && !visitCard.getRecommendedRoute().isEmpty()) {
            zones = Arrays.stream(visitCard.getRecommendedRoute().split(" -> "))
                    .map(String::trim)
                    .filter(zone -> !zone.isEmpty())
                    .collect(Collectors.toList());
        }

        // 3. 특정 Zone 조회 요청인 경우 해당 Zone만 사용
        if (targetZone != null && !targetZone.isBlank()) {
            zones = zones.stream()
                    .filter(zone -> zone.equals(targetZone))
                    .collect(Collectors.toList());
        }

        // 4. Zone별 AI 상품 큐레이션
        List<RouteProductResponse.ZoneRecommendation>
                zoneRecommendations = new ArrayList<>();

        for (String zone : zones) {
            /* 여기서는 해당 Zone 안의 상품 후보 중
             * VisitCard와 가장 잘 어울리는 상품 3개만 AI가 선정한다.
             */
            List<RouteProductResponse.ProductDto> curatedProducts =
                    getAiSelectedProductsForZone(visitCard, zone);

            zoneRecommendations.add(
                    RouteProductResponse.ZoneRecommendation.builder()
                            .zone(zone)
                            .description(
                                    zone
                                            + "에서 "
                                            + visitCard.getUser().getUserName()
                                            + "님의 Visit Card를 분석해 "
                                            + "AI가 엄선한 추천 제품입니다."
                            )
                            .productList(curatedProducts)
                            .build()
            );
        }

        // 5. 최종 Response
        return RouteProductResponse.builder()
                .visitCardId(visitCard.getVisitCardId())
                .recommendedRoutes(zoneRecommendations)
                .build();
    }


    /*
     * 해당 Zone에 존재하는 상품 후보들 중에서 VisitCard와 가장 잘 어울리는 상품 3개를 AI가 선정
     *
     * 판단 기준:
     * 1. 쇼핑 목적
     * 2. 관심 상품 카테고리
     * 3. 오늘의 무드
     * 4. 자유 입력 내용
     * 5. 성별
     * 6. AI 한 줄 무드 요약
     *
     * 방문 시간과 직원 응대 여부는 상품 자체의 적합성보다 낮은 우선순위의 보조 정보로 전달
     */
    private List<RouteProductResponse.ProductDto> getAiSelectedProductsForZone(VisitCard visitCard, String zone) {

        // 1. 해당 Zone의 상품 후보군 확보
        List<ProductEntity> products = productRepository.findAllByZone(zone);

        List<RouteProductResponse.ProductDto> zoneCandidates =
                products.stream()
                        .map(product -> RouteProductResponse.ProductDto.builder()
                                .productId(product.getProductId().longValue())
                                .productName(product.getProductName())
                                .productDetail(product.getProductDetail())
                                .productImg(product.getProductImg())
                                .location(product.getLocation())
                                .stock(product.getStock())
                                .build())
                        .collect(Collectors.toList());

        if (zoneCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 후보 상품이 3개 이하라면 그대로 반환
        if (zoneCandidates.size() <= 3) {
            return new ArrayList<>(zoneCandidates);
        }

        // 2. VisitCard 정보 변환
        String gender = convertGender(visitCard.getGender());
        String interestCategory = convertProductCategory(visitCard.getFindProductCategory());
        String mood = convertMoodCategory(visitCard.getMoodCategory());
        String purpose = visitCard.getPurposeText() != null && !visitCard.getPurposeText().isBlank() ? visitCard.getPurposeText()
                        : "특별한 목적 없이 상품을 둘러보고 싶음";
        String visitTime = visitCard.getVisitTime() != null ? visitCard.getVisitTime().toString()
                        : "정보 없음";
        String supportStatus = convertSupportStatus(visitCard.getSupportStatus());
        String aiMood = visitCard.getAiMood() != null && !visitCard.getAiMood().isBlank() ? visitCard.getAiMood()
                        : "정보 없음";

        // 3. Zone 상품 후보군을 AI가 이해할 수 있는 형태로 구성
        String candidatesInfo = zoneCandidates.stream()
                .map(product -> String.format(
                        """
                        상품 ID: %d
                        상품명: %s
                        상품 상세: %s
                        상품 위치: %s
                        재고: %s
                        """,
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductDetail(),
                        product.getLocation(),
                        product.getStock()
                ))
                .collect(Collectors.joining(
                        "\n------------------------------\n"
                ));

        // 4. AI 프롬프트
        String prompt = String.format(
                """
                너는 MCM 오프라인 매장의 프리미엄 AI 쇼핑 큐레이터다.
                고객의 Visit Card 정보를 분석하고, 이미 결정된 현재 Zone 안에 존재하는 상품 후보들 중 이 고객에게 가장 잘 어울리는 상품을 정확히 3개 선정하라.
                
                [가장 중요한 역할 범위]
                - 현재 Zone은 이미 시스템에서 결정되어 있다.
                - 너는 Zone을 결정하지 않는다.
                - 다른 Zone의 상품을 추천하지 않는다.
                - 반드시 아래 [현재 Zone 상품 후보군]에 존재하는 상품만
                - 추천해야 한다.
                - 상품 후보군에 존재하지 않는 상품 ID를 절대로 만들어내지 마라.
                
                [현재 추천 Zone]
                %s
                
                [고객 Visit Card 정보]
                성별: %s
                관심 상품 카테고리: %s
                오늘의 무드: %s
                쇼핑 목적: %s
                방문 예정 시간: %s
                직원 응대 희망: %s
                AI가 생성한 고객 쇼핑 무드: %s
                
                [현재 Zone의 상품 후보군]
                %s
                
                [상품 선정 방법]
                - 위의 Visit Card와 상품 후보군을 종합적으로 비교하여 고객에게 가장 적합한 상품 3개를 선정하라.
                - 단순히 상품명에 고객의 관심 카테고리와 같은 단어가 포함되어 있는 상품을 선택하지 마라.
                - 상품명과 상품 상세 내용을 함께 분석하고, 고객이 왜 이 매장을 방문했는지를 중심으로 실제 쇼핑 상황에서 가장 매력적인 상품을 판단하라.

                1. 쇼핑 목적
                - 고객의 쇼핑 목적을 가장 중요한 기준으로 사용하라.
                - 고객이 단순히 상품을 구경하려는 것인지, 신상품을 찾는 것인지, 여행용 상품을 찾는 것인지, 선물을 찾는 것인지, 일상에서 사용할 상품을 찾는 것인지 등을 해석하라.
                - 목적에 직접적으로 부합하는 상품을 우선하라.

                2. 관심 상품 카테고리
                - 고객이 선택한 관심 상품 카테고리를 중요한 추천 기준으로 사용하라.
                - 단, 관심 카테고리와 상품명이 정확하게 일치하지 않더라도 상품 상세 내용을 분석했을 때 고객의 목적과 취향에 적합하다면 추천할 수 있다.
                - 반대로 카테고리가 일치하더라도 고객의 목적이나 무드와 크게 맞지 않는다면 우선순위를 낮춰라.

                3. 오늘의 무드
                - 고객의 무드는 상품의 디자인과 스타일을 판단하는 중요한 기준으로 사용하라.
                - 클래식:정제되고 timeless한 분위기, 오래 활용하기 좋은 세련된 스타일을 선호하는 것으로 해석한다.
                - 모던: 깔끔하고 세련되며 현대적인 디자인을 선호하는 것으로 해석한다.
                - 스트리트: 자유롭고 캐주얼하며 개성이 드러나는 스타일을 선호하는 것으로 해석한다.
                - 볼드: 존재감이 강하고 시선을 끄는 디자인을 선호하는 것으로 해석한다.
                - 단, 실제 상품 상세 정보에 없는 특징을 임의로 만들어내지 마라.
                
                4. 자유 입력 목적
                - 고객이 직접 작성한 목적이 있다면 단순히 키워드만 추출하지 마라.
                문장 전체의 의미를 이해하고, 고객이 실제로 원하는 쇼핑 경험을 해석하라.

                예를 들어: "세련되면서 20대 여성이 들기에 과하지 않고 잘 어울리는 제품을 찾으러 왔어요"
                라는 입력이 있다면
                단순히 "여성"이라는 단어만 보는 것이 아니라
                - 세련된 스타일
                - 과하지 않은 디자인
                - 여성 고객에게 자연스럽게 어울림
                등의 의미를 종합하여 상품을 판단하라.

                5. 성별
                - 고객의 성별과 상품의 스타일 및 타깃 적합성을 보조적인 기준으로 고려하라.
                - 단, 성별 하나만을 이유로 상품을 선택하지 마라.

                6. AI Mood
                - AI가 생성한 쇼핑 무드 요약은 고객의 전체적인 쇼핑 의도를 이해하기 위한 보조 정보로 활용하라.
                - 원래 Visit Card의 관심 상품, 무드, 목적보다 AI Mood를 무조건 우선하지 마라.
                
                7. 방문 시간 / 직원 응대 희망
                - 방문 시간과 직원 응대 희망 여부는 상품 자체의 적합성을 판단하는 핵심 기준이 아니다.
                - 상품 선정에 필요한 경우에만 보조적으로 고려하고, 해당 정보 때문에 상품의 순위를 억지로 변경하지 마라.

                [추천 품질 기준]
                ==================================================

                최종 3개 상품은 고객의 취향과 목적을 하나의 일관된 쇼핑 맥락으로 연결해야 한다. 
                단순히 후보군의 앞쪽 상품 3개를 선택하지 마라.
                상품 ID 숫자의 크기나 후보군의 순서는 추천 기준이 아니다.
                가능하다면 3개의 상품이 서로 다른 매력을 보여주면서도 하나의 고객 취향 안에서 자연스럽게 함께 추천될 수 있도록 하라.

                예:
                첫 번째 상품: 고객의 핵심 쇼핑 목적에 가장 직접적으로 부합
                두 번째 상품: 고객의 무드와 스타일에 특히 잘 부합
                세 번째 상품: 고객의 취향을 자연스럽게 확장할 수 있는 선택

                단, 실제 상품 후보군의 특성을 기준으로 판단하고 위 순서를 무조건 따르지는 마라.

                [절대 금지]
                - 현재 Zone 밖의 상품 추천 금지
                - 존재하지 않는 상품 ID 생성 금지
                - 상품 상세에 없는 특징을 사실처럼 생성 금지
                - 동일 상품 중복 추천 금지
                - 상품 후보군의 앞에서부터 3개 선택 금지
                - 상품 ID 순서에 따른 선택 금지
                - 고객 정보와 관계없이 무작위 상품 선택 금지

                [최종 출력 형식]
                반드시 아래와 같이 상품 ID 3개만 출력하라.

                1,3,5

                설명을 작성하지 마라.
                상품명을 작성하지 마라.
                JSON을 작성하지 마라.
                따옴표를 작성하지 마라.
                마크다운을 사용하지 마라.

                반드시 현재 Zone 상품 후보군에 실제로 존재하는
                서로 다른 상품 ID 3개를 쉼표로 구분해서 출력하라.
                """,
                zone,
                gender,
                interestCategory,
                mood,
                purpose,
                visitTime,
                supportStatus,
                aiMood,
                candidatesInfo
        );

        // 5. OpenAI 호출
        String aiResponse = callOpenAI(prompt);

        // 6. AI 응답 파싱
        List<Long> selectedIds = parseProductIds(aiResponse);

        // 7. AI가 반환한 ID가 실제 후보군에 존재하는지 검증
        Set<Long> candidateIds = zoneCandidates.stream()
                .map(RouteProductResponse.ProductDto::getProductId)
                .collect(Collectors.toSet());

        selectedIds = selectedIds.stream()
                .filter(candidateIds::contains)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        // 8. 실제 상품 데이터와 매핑
        Set<Long> validSelectedIds = new HashSet<>(selectedIds);

        List<RouteProductResponse.ProductDto> selectedProducts =
                zoneCandidates.stream()
                        .filter(product ->
                                validSelectedIds.contains(product.getProductId())
                        )
                        .collect(Collectors.toList());

        // 9. AI 결과가 3개 미만이면 후보군에서 보완
        if (selectedProducts.size() < 3) {
            for (RouteProductResponse.ProductDto candidate
                    : zoneCandidates) {
                if (selectedProducts.size() >= 3) {
                    break;
                }
                boolean alreadySelected =
                        selectedProducts.stream()
                                .anyMatch(selected ->
                                        selected.getProductId()
                                                .equals(
                                                        candidate.getProductId()
                                                )
                                );

                if (!alreadySelected) {
                    selectedProducts.add(candidate);
                }
            }
        }
        return selectedProducts;
    }


    //AI 응답에서 상품 ID 추출
    private List<Long> parseProductIds(String aiResponse) {

        List<Long> ids = new ArrayList<>();

        if (aiResponse == null || aiResponse.isBlank()) {
            return ids;
        }

        try {
            String cleanedResponse = aiResponse
                    .trim()
                    .replace("```", "")
                    .replace("json", "")
                    .trim();

            String[] tokens = cleanedResponse.split(",");

            for (String token : tokens) {

                try {
                    ids.add(Long.parseLong(token.trim()));
                } catch (NumberFormatException ignored) {
                    // 숫자가 아닌 값은 무시
                }
            }

        } catch (Exception ignored) {
            return new ArrayList<>();
        }

        return ids;
    }

    // 성별 텍스트 변환
    private String convertGender(Integer gender) {
        if (gender == null) return "정보 없음";

        switch (gender) {
            case 1:
                return "여성";
            case 2:
                return "남성";
            case 3:
                return "기타";
            default:
                return "정보 없음";
        }
    }

    // 관심 상품 카테고리 숫자 -> 텍스트 변환
    private String convertProductCategory(Integer categoryId) {
        if (categoryId == null) return "상품 미지정";
        switch (categoryId) {
            case 1: return "백팩";
            case 2: return "토트백";
            case 3: return "지갑";
            case 4: return "악세서리";
            default: return "기타 상품";
        }
    }

    // 오늘의 무드 숫자 -> 텍스트 변환
    private String convertMoodCategory(Integer categoryId) {
        if (categoryId == null) return "정보 없음";

        switch (categoryId) {
            case 1: return "스트리트";
            case 2: return "클래식";
            case 3: return "모던";
            case 4: return "볼드";
            default: return "미니멀";
        }
    }

    // 직원 응대 선호 숫자 -> 텍스트 변환
    private String convertSupportStatus(Integer statusId) {
        if (statusId == null) return "선택 안 함";
        switch (statusId) {
            case 1: return "응대 받을게요";
            case 2: return "혼자 둘러볼게요";
            case 3: return "30분 후에 받고싶어요";
            default: return "보통";
        }
    }

    /**
     * OpenAI API 호출
     */
    @SuppressWarnings("unchecked")
    public String callOpenAI(String prompt) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);


        Map<String, Object> requestBody =
                new HashMap<>();

        requestBody.put("model", model);


        Map<String, String> message =
                new HashMap<>();

        message.put("role", "user");
        message.put("content", prompt);


        requestBody.put(
                "messages",
                List.of(message)
        );

        requestBody.put(
                "temperature",
                0.3
        );


        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        requestBody,
                        headers
                );


        try {

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            OPENAI_URL,
                            entity,
                            Map.class
                    );


            Map<String, Object> responseBody =
                    response.getBody();


            if (responseBody != null
                    && responseBody.containsKey("choices")) {

                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>)
                                responseBody.get("choices");


                if (!choices.isEmpty()) {

                    Map<String, Object> messageMap =
                            (Map<String, Object>)
                                    choices.get(0)
                                            .get("message");


                    if (messageMap != null) {

                        Object content =
                                messageMap.get("content");

                        if (content != null) {
                            return content.toString().trim();
                        }
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return "";
    }

    public RecommendProductResponse getRecommendedProducts(Integer visitCardId) {

        VisitCard visitCard = visitCardRepository.findById(visitCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 VisitCard ID입니다: " + visitCardId));

        // 기존 추천 동선 결과 조회
        RouteProductResponse routeResponse =
                getRouteProducts(visitCardId, null);

        // Zone별 상품을 하나의 리스트로 변환
        List<RecommendProductResponse.ProductDto> products =
                routeResponse.getRecommendedRoutes()
                        .stream()
                        .flatMap(zone ->
                                zone.getProductList().stream()
                                        .map(product ->
                                                RecommendProductResponse.ProductDto.builder()
                                                        .visitCardId(visitCardId)
                                                        .productId(product.getProductId())
                                                        .productImg(product.getProductImg())
                                                        .zone(zone.getZone())
                                                        .productName(product.getProductName())
                                                        .productDetail(product.getProductDetail())
                                                        .build()
                                        )
                        )
                        .limit(9)
                        .toList();

        List<RecommendedProduct> entities = products.stream()
                .map(dto -> RecommendedProduct.from(dto, visitCard))
                .toList();

        recommendedProductRepository.saveAll(entities);

        return RecommendProductResponse.builder()
                .productList(products)
                .build();
    }
}
