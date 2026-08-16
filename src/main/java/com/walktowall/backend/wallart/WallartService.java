package com.walktowall.backend.wallart;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import com.walktowall.backend.wallart.dto.ReadWallartResponse;
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
import java.util.Base64;

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

    public ReadWallartResponse ReadWallart(Integer userId) {
        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 최근 방문 카드를 찾을 수 없습니다. userId=" + userId));

        Optional<WallartEntity> wallart = wallartRepository.findByVisitCard_VisitCardId(visitCard.getVisitCardId());

        return ReadWallartResponse.builder()
                .message("월아트 이미지 조회를 성공했습니다.")
                .wallartId(wallart.get().getWallartId())
                .wallarImg(wallart.get().getWallartImg())
                .wallartText(wallart.get().getWallartText())
                .build();
    }

    @Transactional
    public CreateWallartResponse createWallart(Integer userId) {
        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 최근 방문 카드를 찾을 수 없습니다. userId=" + userId));

        // 매장 정보
        OfflineStore store = offlineStoreRepository.findById(visitCard.getOfflineStore().getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. id=" + visitCard.getOfflineStore().getStoreId()));
        String storeName = store.getStoreName();

        // 성별 정보
        Integer gender = visitCard.getGender();
        String genderStr;
        if (gender == 1) genderStr = "여성";
        else if (gender == 2) genderStr = "남성";
        else if (gender == 3) genderStr = "사용자가 비공개를 요청함.";
        else genderStr = "확인할 수 없음.";

        // 카테고리 정보
        Integer findProductCategory = visitCard.getFindProductCategory();
        String productTheme = switch (findProductCategory != null ? findProductCategory : 0) {
            case 1 -> "Backpack";
            case 2 -> "Tote bag";
            case 3 -> "Wallet";
            case 4 -> "Accessories";
            default -> "Fashion items";
        };

        // 무드 정보
        Integer moodCategory = visitCard.getMoodCategory();
        String moodCategoryStr;
        if(moodCategory == 1) moodCategoryStr = "스트리트";
        else if(moodCategory == 2) moodCategoryStr = "클래식";
        else if(moodCategory == 3) moodCategoryStr = "모던";
        else if(moodCategory == 4) moodCategoryStr = "볼드";
        else if(moodCategory == 5) moodCategoryStr = "미니멀";
        else moodCategoryStr = "알 수 없음.";
        String aimood = visitCard.getAiMood();

        // 쇼핑 목적
        String purposeText = visitCard.getPurposeText();

        // 방문 예정시간
        LocalDateTime visitTime = visitCard.getVisitTime();

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
        String base64Image = generateImageOpenAI(prompt);
        String localSavedPath = saveBase64Image(base64Image, visitCard.getVisitCardId());

        // 기존에 해당 VisitCard로 등록된 Wallart가 있는지 조회
        Optional<WallartEntity> existingWallart = wallartRepository.findByVisitCard_VisitCardId(visitCard.getVisitCardId());

        WallartEntity wallart;
        if (existingWallart.isPresent()) {
            // 이미 존재한다면 이미지 경로 업데이트 (Dirty Checking을 통해 자동 UPDATE 실행)
            wallart = existingWallart.get();
            wallart.updateWallartImg(localSavedPath); // 또는 별도로 구현한 update 메서드 호출
        } else {
            // 존재하지 않는다면 새로 생성 후 저장 (INSERT)
            wallart = WallartEntity.builder()
                    .visitCard(visitCard)
                    .wallartImg(localSavedPath)
                    .build();
            wallartRepository.save(wallart);
        }
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
        requestBody.put("size", "1792x1024");
        requestBody.put("quality", "auto");

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            OPENAI_IMAGE_URL,
                            entity,
                            Map.class
                    );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException("OpenAI 응답이 비어 있습니다.");
            }

            System.out.println("OpenAI 이미지 생성 응답: " + responseBody);

            Object dataObject = responseBody.get("data");

            if (!(dataObject instanceof List<?> dataList) || dataList.isEmpty()) {
                throw new RuntimeException(
                        "OpenAI 응답에 이미지 데이터가 없습니다. response=" + responseBody
                );
            }

            Object firstObject = dataList.get(0);

            if (!(firstObject instanceof Map<?, ?> imageData)) {
                throw new RuntimeException("OpenAI 이미지 데이터 형식이 올바르지 않습니다.");
            }

            Object b64Object = imageData.get("b64_json");

            if (b64Object == null) {
                throw new RuntimeException(
                        "OpenAI 응답에 b64_json이 없습니다. imageData=" + imageData
                );
            }

            return b64Object.toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이미지 생성 중 오류가 발생했습니다.", e);
        }
    }

    // OpenAI URL로부터 이미지를 다운로드 받아 로컬 디렉터리에 저장
    private String saveBase64Image(String base64Image, Integer visitCardId) {

        try {
            // 디렉터리가 없으면 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 파일명 생성
            String fileName = visitCardId + ".png";

            Path targetPath = uploadPath.resolve(fileName);

            // 이미지 저장
            Files.write(targetPath, imageBytes);

            System.out.println("이미지 저장 완료: " + targetPath);

            return targetPath.toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Base64 이미지를 로컬에 저장하는 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}