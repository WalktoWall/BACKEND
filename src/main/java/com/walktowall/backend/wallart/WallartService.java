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
                "MCM 럭셔리 패션 브랜드 매장 '%s'의 대형 디지털 스크린을 위한 고품격 맞춤형 아트월 배경 이미지. " +
                        "MCM 특유의 프리미엄 가죽 질감, 비세토스 모노그램 패턴, 고급스러운 금속 아키텍처 요소가 현대 예술적으로 어우러진 비주얼 아트워크. " +
                        "디자인 콘셉트 및 스타일: %s 무드와 %s 느낌이 감각적으로 조화를 이루는 초현실적이고 모던한 패션 디스플레이. " +
                        "타겟 및 감성: %s 취향을 반영한 럭셔리 에스테틱. " +
                        "주요 제품 메인 오마주: %s 카테고리의 독창적인 조형미와 실루엣에서 영감을 받은 입체적인 3D 아트 오브제 배치. " +
                        "방문 맥락: '%s'라는 쇼핑 목적에 어울리는 스페셜하고 유니크한 분위기 연출. " +
                        "시간대별 라이팅 및 톤앤매너: %s 방문 시간에 어울리는 입체적이고 세련된 매장 아키텍처 조명 및 은은한 빛 반사 효과. " +
                        "구도 및 레이아웃: 매장 전면 아트월을 압도하는 웅장한 시네마틱 3D 가로형 구도. " +
                        "고객이 전면 인터랙티브 스크린에서 문구를 직접 수정하거나 제품을 커스터마이징하여 배치할 수 있도록, 중앙부와 주요 레이어에는 시각적 간섭이 적고 깔끔하며 여유로운 디스플레이 여백 공간(Spacious center for digital text overlay & product UI)을 가질 것.",
                storeName,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null ? visitTime.getHour() + "시" : "현재 시간대"
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

        headers.set("OpenAI-Organization", "org-EhkLz960bCaI2nG16tUlPLjK");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", imageModel);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("quality", "auto");

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

    // OpenAI URL로부터 이미지를 다운로드 받아 로컬 디렉터리에 저장
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