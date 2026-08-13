package com.walktowall.backend.visitcard;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import com.walktowall.backend.visitcard.dto.VisitCardCreateRequest;
import com.walktowall.backend.visitcard.dto.VisitCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitCardService {
    private final VisitCardRepository visitCardRepository;
    private final UserRepository userRepository;
    private final OfflineStoreRepository offlineStoreRepository;

    // --- [추가된 부분 1] application.yml에서 값 가져오기 ---
    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    //visit card 생성
    @Transactional
    public VisitCardResponse createVisitCard(VisitCardCreateRequest request) {

        // 로그인 기능이 없으므로 고정 사용자 1번을 사용
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 선택한 매장 조회
        OfflineStore offlineStore = offlineStoreRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        //aiMood 저장
        String aiMoodResult = generateAiMood(request, offlineStore.getStoreName());

        VisitCard visitCard = VisitCard.builder()
                .user(user)
                .offlineStore(offlineStore)
                .findProductCategory(request.getFindProductCategory())
                .moodCategory(request.getMoodCategory())
                .purposeText(request.getPurposeText())
                .visitTime(request.getVisitTime())
                .supportStatus(request.getSupportStatus())
                .aiMood(aiMoodResult)
                .build();

        VisitCard savedVisitCard = visitCardRepository.save(visitCard);

        return toResponse(savedVisitCard);
    }

    //visit card 단건 조회
    public VisitCardResponse getVisitCard(Integer visitCardId) {

        VisitCard visitCard = visitCardRepository.findById(visitCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Visit Card입니다."));

        return toResponse(visitCard);
    }

    // --- [추가된 부분 2] OpenAI API 통신 공통 메서드 ---
    public String callOpenAI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model); // yml에 설정한 gpt-4o-mini 적용

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.5);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageMap.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 응답 생성 중 오류가 발생했습니다.";
        }
        return "AI 응답을 받아오지 못했습니다.";
    }

    // 1번: 관심 상품 카테고리 숫자 -> 텍스트 변환
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

    // 2번: 오늘의 무드 숫자 -> 텍스트 변환
    private String convertMoodCategory(Integer categoryId) {
        switch (categoryId) {
            case 1: return "스트리트";
            case 2: return "클래식";
            case 3: return "모던";
            case 4: return "볼드";
            default: return "미니멀";
        }
    }

    // 3번: 직원 응대 선호 숫자 -> 텍스트 변환
    private String convertSupportStatus(Integer statusId) {
        if (statusId == null) return "선택 안 함";
        switch (statusId) {
            case 1: return "응대 받을게요";
            case 2: return "혼자 둘러볼게요";
            case 3: return "30분 후에 받고싶어요";
            default: return "보통";
        }
    }

    public String generateAiMood(VisitCardCreateRequest request, String storeName) {

        // 숫자로 들어온 값을 AI가 이해하기 좋은 텍스트로 변환
        String findProductCategory = convertProductCategory(request.getFindProductCategory());
        String moodCategory = convertMoodCategory(request.getMoodCategory());
        String supportStatus = convertSupportStatus(request.getSupportStatus());

        String purposeText = request.getPurposeText() != null ? request.getPurposeText() : "";
        String visitTime = request.getVisitTime() != null ? request.getVisitTime().toString() : "";

        String promptTemplate = "너는 MCM 오프라인 매장을 위한 프리미엄 AI 컨시어지이자 감각적인 쇼핑 큐레이터이다.\n" +
                        "고객의 Visit Card 정보를 해석해, 오늘 고객이 매장에서 경험할 쇼핑의 목적과 분위기를 감성적이면서도 명확한 한국어 한 문장으로 표현해라.\n\n" +
                        "[고객 Visit Card]\n" +
                        "- 선택 매장: %s\n" +
                        "- 관심 상품 카테고리 및 우선순위: %s\n" +
                        "- 오늘의 무드: %s\n" +
                        "- 쇼핑 목적: %s\n" +
                        "- 방문 예정 시간: %s\n" +
                        "- 직원 응대 선호: %s\n\n" +
                        "[작성 규칙]\n" +
                        "1. 결과는 반드시 한국어 한 문장만 작성한다.\n" +
                        "2. 25자 이상 55자 이하로 작성한다.\n" +
                        "3. \"오늘은 … 날\" 또는 \"…을 위한 …을 발견하는 날\"처럼 방문 경험을 표현하는 문장으로 작성한다.\n" +
                        "4. 고객의 입력값에 포함된 단어만을 그대로 이어 붙여 문장을 만들지 않는다. 각 정보에서 드러나는 고객의 취향과 방문 의도를 해석하고, 여러 정보 사이의 맥락을 연결하여 새로운 의미와 쇼핑 경험을 창의적으로 재구성한다.\n" +
                        "5. 럭셔리 브랜드 컨시어지처럼 세련되고 따뜻한 톤을 유지한다.\n" +
                        "6. 가격, 할인, 재고, 한정판, 실제로 확인되지 않은 매장 서비스는 언급하지 않는다.\n" +
                        "7. 입력에 없는 구체적 상품명, 브랜드 라인, 사실을 만들어내지 않되, 입력된 정보에서 충분히 유추할 수 있는 감정, 상황, 스타일, 기대감은 자유롭게 확장하여 표현한다.\n" +
                        "8. 제목, 따옴표, 이모지, 설명, 줄바꿈을 포함하지 않는다.\n" +
                        "9. 불필요한 설명, 인사말은 절대 포함하지 말고 오직 결과 문장 한 줄만 출력한다.\n" +
                        "10. 관심 상품과 쇼핑 목적을 반드시 문장 속에 자연스럽게 반영하라. 하지만 입력된 값만 사용하고, 비어 있거나 없는 값은 완전히 무시한다." +
                        "11. 관심 상품 카테고리와 쇼핑 목적이 모두 입력된 경우, 두 정보를 연결해 문장에 자연스럽게 반영한다." +
                        "12. 특히 쇼핑 목적에 특정 상품이나 상품 탐색 의도가 포함되어 있다면 이와 관심상품 카테고리를 함께 반영하되, 같은 단어를 그대로 반복하지 않고 자연스럽고 세련된 표현으로 재구성한다.\n\n" +
                        "[출력 예시]\n" +
                        "오늘은 익숙한 클래식에 새로운 설렘을 더하는 특별한 여정을 시작하는 날\n" +
                        "출장을 위한 모던한 백팩 아이템을 발견하는 날\n" +
                        "세련된 일상에 자연스럽게 스며들 지갑과 함께 새로운 매력을 발견하는 날\n" +
                        "오늘은 나만의 감각을 담아 악세서리를 통해 일상을 한층 우아하게 완성하는 날\n" +
                        "담백한 취향 속에서 오래도록 마음에 남을 새로운 선택을 만나는 날" +
                        "오늘은 클래식한 품격 속에서 부모님께 드릴 특별한 백팩 선물을 감각적인 악세서리와 함께 발견하는 날";

        // 변환된 텍스트들을 순서대로 쏙 주입
        String finalPrompt = String.format(
                promptTemplate,
                storeName,
                findProductCategory,
                moodCategory,
                purposeText,
                visitTime,
                supportStatus
        );

        return callOpenAI(finalPrompt).trim();
    }

    private VisitCardResponse toResponse(VisitCard visitCard) {

        return VisitCardResponse.builder()
                .visitCardId(visitCard.getVisitCardId())
                .userId(visitCard.getUser().getUserId())
                .userName(visitCard.getUser().getUserName())
                .storeId(visitCard.getOfflineStore().getStoreId())
                .storeName(visitCard.getOfflineStore().getStoreName())
                .findProductCategory(visitCard.getFindProductCategory())
                .moodCategory(visitCard.getMoodCategory())
                .purposeText(visitCard.getPurposeText())
                .visitTime(visitCard.getVisitTime())
                .supportStatus(visitCard.getSupportStatus())
                .aiMood(visitCard.getAiMood())
                .createdAt(visitCard.getCreatedAt())
                .build();
    }
}


