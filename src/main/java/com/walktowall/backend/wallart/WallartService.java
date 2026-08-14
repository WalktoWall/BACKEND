package com.walktowall.backend.wallart;

import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.wallart.dto.CreateWallartRequest;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WallartService {
    private final VisitCardRepository visitCardRepository;
    private final OfflineStoreRepository offlineStoreRepository;
    private final WallartRepository wallartRepository;

    // open ai api 사용
    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Transactional(readOnly = true)
    public CreateWallartResponse createWallart(CreateWallartRequest request) {
        // 매장 정보
        String storeName = offlineStoreRepository.findById(request.getVisitCard().getStoreId()).get().getStoreName();

        // 성별 정보
        Integer gender = request.getVisitCard().getGender();
        String genderStr;
        if (gender == 1) genderStr = "여성";
        else if (gender == 2) genderStr = "남성";
        else if (gender == 3) genderStr = "사용자가 비공개를 요청함.";
        else genderStr = "확인할 수 없음.";

        // 카테고리 정보
        Integer findProductCategory = request.getVisitCard().getFindProductCategory();
        String findProductCategoryStr;
        if(findProductCategory == 1) findProductCategoryStr = "백팩";
        else if (findProductCategory == 2) findProductCategoryStr = "토트백";
        else if (findProductCategory == 3) findProductCategoryStr = "지갑";
        else if (findProductCategory == 4) findProductCategoryStr = "악세서리";
        else findProductCategoryStr = "알 수 없음.";

        // 무드 정보
        Integer moodCategory = request.getVisitCard().getMoodCategory();
        String moodCategoryStr;
        if(moodCategory == 1) moodCategoryStr = "스트리트";
        else if(moodCategory == 2) moodCategoryStr = "클래식";
        else if(moodCategory == 3) moodCategoryStr = "모던";
        else if(moodCategory == 4) moodCategoryStr = "볼드";
        else if(moodCategory == 5) moodCategoryStr = "미니멀";
        else moodCategoryStr = "알 수 없음.";
        String aimood = request.getVisitCard().getAiMood();

        // 쇼핑 목적
        String purposeText = request.getVisitCard().getPurposeText();

        // 방문 예정시간
        LocalDateTime visitTime = request.getVisitCard().getVisitTime();

        return new CreateWallartResponse(); // 수정 예정
    }
}
