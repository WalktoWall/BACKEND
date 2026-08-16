package com.walktowall.backend.staff;

import com.walktowall.backend.staff.dto.StaffCustomerResponse;
import com.walktowall.backend.staff.dto.StaffProductResponse;
import com.walktowall.backend.staff.dto.StaffVisitResponse;
import com.walktowall.backend.visitcard.RecommendedProduct;
import com.walktowall.backend.visitcard.RecommendedProductRepository;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.visitcard.service.RecommendService;
import com.walktowall.backend.store.OfflineStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final VisitCardRepository visitCardRepository;
    private final RecommendedProductRepository recommendedProductRepository; // 🌟 추가된 Repository 주입

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";


    // 오늘 방문 예정 고객 목록
    public List<StaffCustomerResponse> getTodayCustomers() {

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<VisitCard> visitCards = visitCardRepository.findByVisitTimeBetween(startOfDay, endOfDay);

        // 사용자별 가장 최신 VisitCard만 남김
        Map<Integer, VisitCard> latestVisitCards =
                visitCards.stream()
                        .collect(Collectors.toMap(
                                visitCard ->
                                        visitCard.getUser().getUserId(),

                                visitCard -> visitCard,

                                (existing, replacement) ->
                                        replacement.getVisitTime()
                                                .isAfter(
                                                        existing.getVisitTime()
                                                )
                                                ? replacement
                                                : existing
                        ));

        return latestVisitCards.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                VisitCard::getVisitTime
                        )
                )
                .map(this::toCustomerResponse)
                .toList();
    }


    // 직원용 Visit Card 상세 조회
    public StaffVisitResponse getVisitDetail(Integer visitCardId) {
        VisitCard visitCard =
                visitCardRepository.findById(visitCardId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 Visit Card입니다."
                                )
                        );

        OfflineStore store =
                visitCard.getOfflineStore();

        // 추천 동선
        List<String> recommendedRoute = parseRecommendedRoute(visitCard.getRecommendedRoute());

        // 🌟 DB에 저장된 추천 상품을 조회하도록 변경
        List<StaffProductResponse> startProducts = getSavedRecommendedProducts(visitCardId);

        // AI 직원 응대 가이드
        String staffGuidance = generateStaffGuidance(visitCard);

        return StaffVisitResponse.builder()
                .visitCardId(visitCard.getVisitCardId())
                .userId(visitCard.getUser().getUserId())
                .userName(visitCard.getUser().getUserName())
                .storeName(store.getStoreName())
                .visitTime(visitCard.getVisitTime())
                .gender(convertGender(visitCard.getGender()))
                .findProductCategory(convertProductCategory(visitCard.getFindProductCategory()))
                .moodCategory(convertMoodCategory(visitCard.getMoodCategory()))
                .purposeText(visitCard.getPurposeText())
                .aiMood(visitCard.getAiMood())
                .recommendedRoute(recommendedRoute)
                .startRecommendedProducts(startProducts)
                .staffGuidance(staffGuidance)
                .build();
    }


    // 오늘 방문 고객 목록 Response 변환
    private StaffCustomerResponse toCustomerResponse(VisitCard visitCard) {
        return StaffCustomerResponse.builder()
                .userId(visitCard.getUser().getUserId())
                .userName(visitCard.getUser().getUserName())
                .visitCardId(visitCard.getVisitCardId())
                .visitTime(visitCard.getVisitTime())
                .storeName(visitCard.getOfflineStore().getStoreName())
                .build();
    }


    // 추천 동선 문자열 파싱
    private List<String> parseRecommendedRoute(String route) {
        if (route == null || route.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(
                        route.split(" -> ")
                )
                .map(String::trim)
                .filter(zone -> !zone.isEmpty())
                .toList();
    }


    // 🌟 DB에 저장되어 있는 추천 상품 리스트를 조회하여 StaffProductResponse로 변환
    private List<StaffProductResponse> getSavedRecommendedProducts(Integer visitCardId) {
        List<RecommendedProduct> recommendedProducts =
                recommendedProductRepository.findByVisitCard_VisitCardId(visitCardId);

        return recommendedProducts.stream()
                .map(product -> StaffProductResponse.builder()
                        .zone(product.getProductZone())
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .productImg(product.getProductImg())
                        .productDetail(product.getProductDetail())
                        .build()
                )
                .toList();
    }


    // AI 직원 응대 가이드 생성
    private String generateStaffGuidance(VisitCard visitCard) {
        String gender = convertGender(visitCard.getGender());

        String category = convertProductCategory(visitCard.getFindProductCategory());

        String mood = convertMoodCategory(visitCard.getMoodCategory());

        String purpose = visitCard.getPurposeText();

        if (purpose == null || purpose.isBlank()) {
            purpose = "특별한 목적 없이 상품을 둘러보고 싶어함";
        }

        String aiMood = visitCard.getAiMood();

        if (aiMood == null || aiMood.isBlank()) {
            aiMood = "정보 없음";
        }

        String supportStatus = convertSupportStatus(visitCard.getSupportStatus());

        String prompt = String.format(
                """
                너는 MCM 오프라인 매장의 전문 퍼스널 쇼핑 어드바이저다.

                고객의 Visit Card 정보를 분석하여
                직원이 실제 매장에서 고객을 응대할 때 참고할 수 있는
                간결하고 자연스러운 직원 응대 가이드를 작성하라.

                [고객 정보]

                성별: %s
                관심 상품: %s
                오늘의 무드: %s
                쇼핑 목적: %s
                직원 응대 희망: %s
                AI 쇼핑 무드: %s

                [작성 목적]

                직원이 고객의 Visit Card를 보고 "이 고객에게 어떤 방식으로 다가가야 하는가?" 를 바로 이해할 수 있어야 한다.
                고객의 정보를 단순히 나열하지 말고, 전체 정보를 하나의 쇼핑 맥락으로 해석하라.
                특히 쇼핑 목적과 자유 입력 내용을 가장 중요하게 고려하고, 무드와 관심 상품을 함께 활용하라.
                직원 응대 희망 여부도 고려하되 고객에게 부담스러운 응대를 강요하는 방식으로 작성하지 마라.

                [출력 형식]

                반드시 정확히 3개의 문장으로 작성하라.

                첫 번째 문장:
                고객의 현재 쇼핑 의도와 취향을 직원이 빠르게 이해할 수 있도록 고객을 한 문장으로 요약하라.

                두 번째 문장:
                이 고객에게 직원이 어떤 방식으로 응대하면 좋은지 방식이나 과정을 구체적으로 추천하라.

                세 번째 문장:
                직원이 고객에게 실제로 처음 건넬 수 있는 자연스러운 예의 있는 말투로 첫 응대 문장을 작성하라.

                [중요]
                - 설명이나 제목을 붙이지 마라.
                - "고객 요약:", "응대 추천:", "첫 응대:" 같은 라벨을 붙이지 마라.
                - 정확히 3개의 문장만 출력하라.
                - 각 문장은 "- " 표시를 시작으로 출력하며, 줄바꿈으로 구분하라.
                - 상품이나 고객의 특징을 근거 없이 만들어내지 마라.
                - 고객의 Visit Card에 없는 취향을 단정하지 마라.
                - 지나치게 판매를 강요하는 표현을 사용하지 마라.
                - 실제 MCM 매장 직원이 사용할 수 있을 정도로 자연스럽게 작성하라.

                """,
                gender,
                category,
                mood,
                purpose,
                supportStatus,
                aiMood
        );

        return callOpenAI(prompt);
    }


    // OpenAI 호출
    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("model", model);

        Map<String, String> message = new HashMap<>();

        message.put("role", "user");

        message.put("content", prompt);

        requestBody.put("messages", List.of(message));

        requestBody.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (!choices.isEmpty()) {

                    Map<String, Object> messageMap =(Map<String, Object>) choices.get(0).get("message");

                    if (messageMap != null) {
                        Object content = messageMap.get("content");

                        if (content != null) {
                            return content.toString().trim();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "고객의 Visit Card 정보를 확인하고 편안한 분위기에서 응대해주세요.";
    }


    private String convertGender(Integer gender) {

        if (gender == null) {
            return "정보 없음";
        }

        return switch (gender) {
            case 1 -> "여성";
            case 2 -> "남성";
            case 3 -> "기타";
            default -> "정보 없음";
        };
    }


    private String convertProductCategory(
            Integer categoryId
    ) {

        if (categoryId == null) {
            return "상품 미지정";
        }

        return switch (categoryId) {
            case 1 -> "백팩";
            case 2 -> "토트백";
            case 3 -> "지갑";
            case 4 -> "악세서리";
            default -> "기타 상품";
        };
    }


    private String convertMoodCategory(
            Integer categoryId
    ) {

        if (categoryId == null) {
            return "정보 없음";
        }

        return switch (categoryId) {
            case 1 -> "스트리트";
            case 2 -> "클래식";
            case 3 -> "모던";
            case 4 -> "볼드";
            case 5 -> "미니멀";
            default -> "정보 없음";
        };
    }


    private String convertSupportStatus(
            Integer statusId
    ) {

        if (statusId == null) {
            return "선택 안 함";
        }

        return switch (statusId) {
            case 1 -> "응대 받을게요";
            case 2 -> "혼자 둘러볼게요";
            case 3 -> "30분 후에 받고 싶어요";
            default -> "정보 없음";
        };
    }
}