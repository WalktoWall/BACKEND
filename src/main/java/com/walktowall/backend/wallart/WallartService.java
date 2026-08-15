package com.walktowall.backend.wallart;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.wallart.dto.CreateWallartRequest;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Value("${openai.image-model}")
    private String imageModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_IMAGE_URL = "https://api.openai.com/v1/images/generations";

    // 로컬 이미지 저장 위치
    private final String UPLOAD_DIR = "uploads/wallarts/";

    @Transactional
    public CreateWallartResponse createWallart(CreateWallartRequest request) {
        // 매장 정보
        Integer storeId = request.getVisitCard().getStoreId();
        OfflineStore store = offlineStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. id=" + storeId));
        String storeName = store.getStoreName();

        // 성별 정보
        Integer gender = request.getVisitCard().getGender();
        String genderStr;
        if (gender == 1) genderStr = "여성";
        else if (gender == 2) genderStr = "남성";
        else if (gender == 3) genderStr = "사용자가 비공개를 요청함.";
        else genderStr = "확인할 수 없음.";

        // 카테고리 정보
        Integer findProductCategory = request.getVisitCard().getFindProductCategory();
        String productTheme = switch (findProductCategory != null ? findProductCategory : 0) {
            case 1 -> "Backpack";
            case 2 -> "Tote bag";
            case 3 -> "Wallet";
            case 4 -> "Accessories";
            default -> "Fashion items";
        };

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

        // 프롬포트
        String prompt = String.format(
                "An artistic wall art design for a store named '%s'. Style: %s and %s, Product theme: %s.",
                storeName, moodCategoryStr, aimood, productTheme
        );

        // 이미지 생성 API 호출 (생성된 이미지 URL을 반환받음)
        String tempImageUrl = generateImageOpenAI(prompt);
        String localSavedPath = downloadAndSaveImage(tempImageUrl, request.getVisitCard().getVisitCardId());
        VisitCard visitCard = visitCardRepository.findById(request.getVisitCard().getVisitCardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방문 카드를 찾을 수 없습니다. id=" + request.getVisitCard().getVisitCardId()));

        WallartEntity wallart = WallartEntity.builder()
                        .visitCard(visitCard)
                        .wallartImg(localSavedPath)
                        .build();
        wallartRepository.save(wallart);
        System.out.println("로컬에 저장된 이미지 경로: " + localSavedPath);

        return CreateWallartResponse.builder()
                .message("월아트 이미지 생성을 성공하였습니다.")
                .wallartId(wallart.getWallartId())
                .build();
    }

    // open ai를 통한 이미지 생성 메소드(이미지 생성 url을 반환, url은 1시간 유효)
    public String generateImageOpenAI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", imageModel);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("quality", "standard");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_IMAGE_URL, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseBody.get("data");
                if (!dataList.isEmpty()) {
                    return (String) dataList.get(0).get("url");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이미지 생성 중 오류가 발생했습니다.", e);
        }
        throw new RuntimeException("이미지 URL을 받아오지 못했습니다.");
    }

    /**
     * 2. OpenAI URL로부터 이미지를 다운로드 받아 로컬 디렉터리에 저장
     * @param imageUrl OpenAI가 반환한 임시 이미지 URL
     * @return 로컬에 저장된 파일 경로 (또는 파일명)
     */
    private String downloadAndSaveImage(String imageUrl, Integer visitCardId) {
        try {
            // 디렉터리가 없으면 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성 (visitCardId.png)
            String fileName = visitCardId + ".png";
            Path targetPath = uploadPath.resolve(fileName);

            // URL 스트림을 통해 이미지 다운로드 및 로컬 파일로 복사
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 저장된 로컬 상대 경로 또는 절대 경로 반환
            return targetPath.toString(); // 예: "uploads/wallarts/a1b2c3d4-....png"

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("로컬에 이미지를 저장하는 중 오류가 발생했습니다.", e);
        }
    }
}