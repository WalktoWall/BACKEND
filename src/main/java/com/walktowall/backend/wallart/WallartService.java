package com.walktowall.backend.wallart;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import com.walktowall.backend.wallart.dto.CreateWallartResponse;
import com.walktowall.backend.wallart.dto.EditWallartTextResponse;
import com.walktowall.backend.wallart.dto.ReadWallartResponse;
import com.walktowall.backend.wallart.dto.RecommendWallartTextResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Value("${openai.model}")
    private String model;

    @Value("${openai.image-model}")
    private String imageModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_IMAGE_URL = "https://api.openai.com/v1/images/generations";
    private final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";

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

        String prompt2 = String.format(
                """
                MCM 럭셔리 패션 브랜드 매장의 디지털 아트월에 배치할
                프리미엄 패션 문구 1개를 추천해줘.
    
                [고객 정보]
                - 매장: %s
                - 오늘의 무드: %s
                - AI 무드: %s
                - 고객 성별: %s
                - 주요 제품: %s
                - 쇼핑 목적: %s
                - 방문 시간: %s
    
                [문구 생성 기준]
    
                반드시 '오늘의 무드'를 가장 중요한 기준으로 사용한다.
    
                [스트리트]
                - 대담하고 즉흥적인 에너지
                - 도시와 움직임, 자유로운 자기표현
                - 짧은 명령형 또는 선언형 문장 2개
                - 두 문장을 마침표로 끊어 리듬감을 줄 것
                - 영문 기준 12단어 이내
                - 핵심 어휘: Move, Bold, Rule, Own, Street, Fear Less
    
                예시:
                "Move Bold. Own Your Journey."
                "Fear Less. Move More."
    
                [클래식]
                - 우아하고 시간을 초월한 정서
                - 이야기와 유산, 품격의 이미지
                - 완결된 서술형 한 문장
                - 부드럽고 자연스럽게 흐르는 구조
                - 핵심 어휘: Story, Timeless, Carry, Legacy, Grace
    
                예시:
                "A Story Worth Carrying."
                "Elegance Never Fades."
    
                [모던]
                - 절제되고 미니멀한 감각
                - 군더더기 없는 세련된 표현
                - A, B 형태의 짧은 대구 구조
                - 쉼표를 활용하여 대비를 강조
                - 핵심 어휘: Simple, Clean, Clear, Less, Structured
    
                예시:
                "Less Noise, More You."
                "Clean Lines, Clear Mind."
    
                [볼드]
                - 강렬한 확신과 자기표현
                - 짧고 강한 단언형 문장
                - 단어 수를 최소화하여 임팩트를 극대화
                - 핵심 어휘: Unapologetic, Bold, Own, Statement, Loud
    
                예시:
                "Unapologetically You."
                "Bold Moves Only."
    
                [목적에 따른 보정]
                쇼핑 목적이 선물과 관련된 경우
                '전하다', '간직하다', '특별한 순간'의 의미가 자연스럽게 드러나도록
                Carry, Keep, Give, Moment 등의 어휘를 적절히 활용한다.
    
                [제품에 따른 보정]
                제품명을 직접적으로 반복하지 않는다.
                해당 제품의 실루엣, 스타일, 움직임 또는 소유의 의미를
                문구에 자연스럽게 반영한다.
    
                [브랜드 톤]
                MCM의 럭셔리 패션 브랜드 이미지에 어울리는
                세련되고 현대적인 캠페인 카피처럼 작성한다.
                지나치게 상업적인 광고 문구는 피한다.
    
                [출력 규칙]
                - 반드시 정확히 1개의 문구를 생성한다.
                - 문구는 영어로 작성한다.
                - 문법적으로 자연스러워야 한다.
                - 문구 앞뒤에 따옴표를 붙이지 않는다.
                - 설명이나 번호를 포함하지 않는다.
                - 반드시 JSON 배열 하나만 반환한다.
    
                반환 형식:
                ["문구1"]
                """,
                storeName,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null
                        ? visitTime.getHour() + "시"
                        : "현재 시간대"
        );

        List<String> phrases = generateTextOpenAI(prompt2);

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
                    .wallartText(phrases.get(0))
                    .build();
            wallartRepository.save(wallart);
        }
        return CreateWallartResponse.builder()
                .message("월아트 이미지 생성을 성공하였습니다.")
                .wallartId(wallart.getWallartId())
                .build();
    }

    public RecommendWallartTextResponse recommendWallartText(Integer userId) {
        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 최근 방문 카드를 찾을 수 없습니다. userId=" + userId));
        Optional<WallartEntity> wallart = wallartRepository.findByVisitCard_VisitCardId(visitCard.getVisitCardId());

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
                """
                MCM 럭셔리 패션 브랜드 매장의 디지털 아트월에 배치할
                프리미엄 패션 문구 5개를 추천해줘.
    
                [고객 정보]
                - 매장: %s
                - 오늘의 무드: %s
                - AI 무드: %s
                - 고객 성별: %s
                - 주요 제품: %s
                - 쇼핑 목적: %s
                - 방문 시간: %s
    
                [문구 생성 기준]
    
                반드시 '오늘의 무드'를 가장 중요한 기준으로 사용한다.
    
                [스트리트]
                - 대담하고 즉흥적인 에너지
                - 도시와 움직임, 자유로운 자기표현
                - 짧은 명령형 또는 선언형 문장 2개
                - 두 문장을 마침표로 끊어 리듬감을 줄 것
                - 영문 기준 12단어 이내
                - 핵심 어휘: Move, Bold, Rule, Own, Street, Fear Less
    
                예시:
                "Move Bold. Own Your Journey."
                "Fear Less. Move More."
    
                [클래식]
                - 우아하고 시간을 초월한 정서
                - 이야기와 유산, 품격의 이미지
                - 완결된 서술형 한 문장
                - 부드럽고 자연스럽게 흐르는 구조
                - 핵심 어휘: Story, Timeless, Carry, Legacy, Grace
    
                예시:
                "A Story Worth Carrying."
                "Elegance Never Fades."
    
                [모던]
                - 절제되고 미니멀한 감각
                - 군더더기 없는 세련된 표현
                - A, B 형태의 짧은 대구 구조
                - 쉼표를 활용하여 대비를 강조
                - 핵심 어휘: Simple, Clean, Clear, Less, Structured
    
                예시:
                "Less Noise, More You."
                "Clean Lines, Clear Mind."
    
                [볼드]
                - 강렬한 확신과 자기표현
                - 짧고 강한 단언형 문장
                - 단어 수를 최소화하여 임팩트를 극대화
                - 핵심 어휘: Unapologetic, Bold, Own, Statement, Loud
    
                예시:
                "Unapologetically You."
                "Bold Moves Only."
    
                [목적에 따른 보정]
                쇼핑 목적이 선물과 관련된 경우
                '전하다', '간직하다', '특별한 순간'의 의미가 자연스럽게 드러나도록
                Carry, Keep, Give, Moment 등의 어휘를 적절히 활용한다.
    
                [제품에 따른 보정]
                제품명을 직접적으로 반복하지 않는다.
                해당 제품의 실루엣, 스타일, 움직임 또는 소유의 의미를
                문구에 자연스럽게 반영한다.
    
                [브랜드 톤]
                MCM의 럭셔리 패션 브랜드 이미지에 어울리는
                세련되고 현대적인 캠페인 카피처럼 작성한다.
                지나치게 상업적인 광고 문구는 피한다.
    
                [출력 규칙]
                - 반드시 정확히 5개의 문구를 생성한다.
                - 모든 문구는 영어로 작성한다.
                - 5개 문구는 서로 다른 표현을 사용한다.
                - 문법적으로 자연스러워야 한다.
                - 문구 앞뒤에 따옴표를 붙이지 않는다.
                - 설명이나 번호를 포함하지 않는다.
                - 반드시 JSON 배열 하나만 반환한다.
    
                반환 형식:
                ["문구1", "문구2", "문구3", "문구4", "문구5"]
                """,
                storeName,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null
                        ? visitTime.getHour() + "시"
                        : "현재 시간대"
        );

        List<String> phrases = generateTextOpenAI(prompt);

        return RecommendWallartTextResponse.builder()
                .message("월아트 문구 추천 조회에 성공했습니다.")
                .textList(phrases)
                .build();
    }

    @Transactional
    public EditWallartTextResponse updateWallartText(Integer userId, String text) {
        VisitCard visitCard = visitCardRepository.findFirstByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 최근 방문 카드를 찾을 수 없습니다. userId=" + userId));

        WallartEntity wallart = wallartRepository.findByVisitCard_VisitCardId(visitCard.getVisitCardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방문 카드의 월아트를 찾을 수 없습니다. visitCardId=" + visitCard.getVisitCardId()));
        wallart.setWallartText(text);

        return new EditWallartTextResponse("월아트 문구 수정을 성공하였습니다.");
    }

    private List<String> parsePhrases(String response) {

        try {
            return objectMapper.readValue(
                    response,
                    new TypeReference<List<String>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "AI 문구 응답 파싱에 실패했습니다. response=" + response,
                    e
            );
        }
    }


    public List<String> generateTextOpenAI(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("model", model);

        requestBody.put(
                "messages",
                List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                "당신은 MCM 럭셔리 패션 브랜드의 캠페인 카피라이터입니다. " +
                                        "사용자가 제공한 무드와 조건을 바탕으로 세련된 영문 패션 문구를 작성합니다."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        requestBody.put("temperature", 0.9);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            OPENAI_CHAT_URL,
                            entity,
                            Map.class
                    );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException("OpenAI 응답이 비어 있습니다.");
            }

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) responseBody.get("choices");

            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException(
                        "OpenAI 응답에 choices가 없습니다."
                );
            }

            Map<String, Object> firstChoice = choices.get(0);

            Map<String, Object> message =
                    (Map<String, Object>) firstChoice.get("message");

            String content = message.get("content").toString();

            return parsePhrases(content);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "문구 생성 중 오류가 발생했습니다.",
                    e
            );
        }
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