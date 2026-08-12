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

        VisitCard visitCard = VisitCard.builder()
                .user(user)
                .offlineStore(offlineStore)
                .findProductCategory(request.getFindProductCategory())
                .moodCategory(request.getMoodCategory())
                .purposeText(request.getPurposeText())
                .visitTime(request.getVisitTime())
                .supportStatus(request.getSupportStatus())
                .aiMood(null) // AI 연동 전까지는 빈 값
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
        requestBody.put("temperature", 0.7);

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


