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
    // private final String UPLOAD_DIR = "uploads/wallarts/";

    @Value("${file.upload-dir}")
    private String UPLOAD_DIR;

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
                """
                MCM 럭셔리 패션 브랜드 매장 '%s'의 대형 가로형 디지털 아트월을 위한
                고품격 맞춤형 패션 아트워크를 생성한다.
        
                실제 MCM 플래그십 스토어 내부에 설치된 대형 디지털 월아트처럼 보이는
                건축적이고 조형적인 럭셔리 패션 아트 인스톨레이션을 표현한다.
        
                단순한 제품 광고 이미지나 패션 화보가 아니다.
                건축 공간, 소재, 조명, 그림자, 조형 오브제가 하나의 작품처럼 구성된
                프리미엄 공간 중심의 비주얼을 생성한다.
        
                사람, 모델, 쇼핑객, 직원, 쇼핑백 등 인간 중심의 요소는 사용하지 않는다.
                제품을 직접 촬영한 상품 광고처럼 표현하지 않는다.
        
        
                [고객 정보]
        
                - 오늘의 무드: %s
                - AI 무드: %s
                - 고객 성별: %s
                - 주요 제품 카테고리: %s
                - 쇼핑 목적: %s
                - 방문 시간: %s
        
        
                ============================================================
                [MOOD PRIORITY — 가장 중요한 생성 규칙]
                ============================================================
        
                오늘의 무드 '%s'를 이미지 생성의 최우선 기준으로 사용한다.
        
                각 무드는 반드시 아래의 기능명세서 기준을 따른다.
        
                고객 성별, 쇼핑 목적, 방문 시간, 제품 카테고리는
                무드의 기본적인 색감, 공간, 조명, 구도를 변경하지 않는다.
        
                고객 정보는 선택된 무드 안에서 소재, 오브제의 형태,
                공간의 세부적인 분위기를 조정하는 보조 조건으로만 사용한다.
        
                AI 무드 '%s'는 오늘의 무드를 보완하는 참고 조건으로 사용한다.
                단, AI 무드가 오늘의 무드와 충돌할 경우
                반드시 오늘의 무드를 우선한다.
        
        
                ============================================================
                [MOOD FUNCTION SPECIFICATION]
                ============================================================
        
        
                [스트리트]
        
                오늘의 무드가 '스트리트'인 경우
                강렬하고 도시적인 현대 럭셔리 공간을 표현한다.
        
                색감:
                - 다크 톤을 중심으로 구성한다.
                - 블랙과 실버의 강한 대비를 사용한다.
                - 네온 또는 크롬 소재의 포인트를 제한적으로 사용한다.
                - 전체적으로 어둡고 도시적인 색조를 유지한다.
                - 네온 컬러가 이미지 전체를 지배하지 않도록 한다.
        
                공간/소재:
                - 콘크리트 건축 구조물
                - 거친 금속 표면
                - 메탈 구조물
                - 도심의 야경
                - 루프탑 또는 어두운 도심 공간
                - 그래피티에서 영감을 받은 도시적인 텍스처
                - 산업적인 소재와 고급스러운 럭셔리 소재의 대비
                - 젖은 콘크리트 또는 석재 바닥을 사용할 수 있다.
        
                조명:
                - 인공조명을 중심으로 구성한다.
                - 네온사인 또는 네온 컬러의 간접조명을 제한적으로 사용한다.
                - 강한 명암 대비를 사용한다.
                - 깊은 그림자를 표현한다.
                - 금속과 바닥에 빛이 반사되는 효과를 표현한다.
        
                구도:
                - 로우앵글 또는 낮은 시점을 사용한다.
                - 사선 구조와 비대칭적인 건축 요소를 활용한다.
                - 정적인 공간보다 움직임과 에너지가 느껴지는
                  다이나믹한 프레이밍을 사용한다.
                - 도시적이고 대담한 공간감을 표현한다.
                - 전경과 후경의 건축 구조를 겹쳐 깊이감 있는 공간을 만든다.
        
        
                [클래식]
        
                오늘의 무드가 '클래식'인 경우
                따뜻하고 시간을 초월한 럭셔리 공간을 표현한다.
        
                색감:
                - 웜톤을 중심으로 구성한다.
                - 브라운, 골드, 베이지 계열을 중심으로 한다.
                - 따뜻하고 차분한 색조를 사용한다.
                - 전체적으로 고급스럽고 안정적인 색감을 유지한다.
                - 지나치게 강한 컬러 대비를 사용하지 않는다.
        
                공간/소재:
                - 마블 또는 트래버틴 스톤 벽면
                - 석재 바닥
                - 브론즈 또는 골드 메탈 좌대
                - 고급스러운 가죽 텍스처
                - 클래식하면서도 현대적인 럭셔리 인테리어
                - 안정감 있는 기하학적 형태의 오브제
                - 대형 석재 벽면과 건축적인 구조물을 활용한다.
        
                조명:
                - 부드러운 자연광 또는 간접조명을 사용한다.
                - 낮은 명암 대비를 유지한다.
                - 은은하고 따뜻한 빛의 확산을 표현한다.
                - 따뜻한 빛이 석재와 금속 표면에 자연스럽게 반사되도록 한다.
                - 지나치게 강한 그림자나 극적인 조명을 피한다.
        
                구도:
                - 정면을 바라보는 안정적인 구도를 우선한다.
                - 대칭적인 공간 구성을 우선한다.
                - 좌대와 오브제를 균형 있고 정돈되게 배치한다.
                - 수직과 수평의 건축선을 안정적으로 표현한다.
                - 웅장하지만 과하지 않은 클래식한 공간감을 표현한다.
        
        
                [모던]
        
                오늘의 무드가 '모던'인 경우
                정제되고 현대적인 럭셔리 건축 공간을 표현한다.
        
                색감:
                - 무채색을 중심으로 구성한다.
                - 화이트와 그레이를 기본 색상으로 사용한다.
                - 포인트 컬러는 최대 1개만 제한적으로 사용한다.
                - 전체적으로 깨끗하고 절제된 색감을 유지한다.
        
                공간/소재:
                - 유리
                - 콘크리트
                - 매끈하고 균일한 표면
                - 미니멀한 건축 구조
                - 넓고 비어 있는 공간
                - 정제된 기하학적 형태
                - 불필요한 장식이 없는 현대적인 럭셔리 공간
                - 건축 구조와 오브제 사이에 충분한 여백을 둔다.
        
                조명:
                - 균일하고 밝은 조명을 사용한다.
                - 그림자를 최소화한다.
                - 공간 전체에 고르게 퍼지는 깨끗한 조명을 표현한다.
                - 표면의 질감과 구조가 명확하게 보이도록 한다.
                - 과도하게 극적인 명암 대비는 피한다.
        
                구도:
                - 중앙 정렬을 중심으로 구성한다.
                - 네거티브 스페이스를 강조한다.
                - 오브제와 건축 요소 사이에 충분한 여백을 둔다.
                - 정돈되고 균형 잡힌 프레이밍을 사용한다.
                - 미니멀하면서도 고급스러운 공간감을 표현한다.
        
        
                [볼드]
        
                오늘의 무드가 '볼드'인 경우
                강렬하고 예술적인 럭셔리 공간을 표현한다.
        
                색감:
                - 원색 대비를 적극적으로 사용한다.
                - 블랙을 기본 색상으로 사용한다.
                - 비비드한 레드 또는 골드 컬러를 강한 포인트로 사용한다.
                - 색상 대비가 한눈에 느껴지도록 구성한다.
                - 포인트 컬러는 특정 건축 요소 또는 오브제에 집중한다.
        
                공간/소재:
                - 금속 소재
                - 벨벳
                - 광택이 있는 고급 소재
                - 거칠거나 매끈한 표면의 강한 대비
                - 조형적이고 예술적인 오브제
                - 대담한 형태의 건축 구조물
                - 거대한 벽체 또는 기둥을 활용할 수 있다.
        
                조명:
                - 스포트라이트 중심의 극적인 조명을 사용한다.
                - 밝은 영역과 어두운 영역의 강한 대비를 표현한다.
                - 특정 오브제에 강렬한 빛을 집중시킨다.
                - 빛과 그림자가 극적으로 교차하는 분위기를 만든다.
                - 레드 또는 골드 조명이 공간의 특정 부분을 강조하도록 한다.
        
                구도:
                - 클로즈업에 가까운 과감한 프레이밍을 사용한다.
                - 오브제 또는 건축 구조를 크게 확대하여 보여준다.
                - 일부 요소가 화면 밖으로 과감하게 잘리는 구도를 허용한다.
                - 비대칭적인 구도를 적극적으로 활용한다.
                - 파격적이고 강렬한 시각적 임팩트를 표현한다.
        
        
                [미니멀]
        
                오늘의 무드가 '미니멀'인 경우
                절제된 갤러리 또는 현대 미술관과 같은 공간을 표현한다.
        
                색감:
                - 무채색과 오프화이트를 중심으로 구성한다.
                - 채도를 최대한 낮춘 차분한 색감을 사용한다.
                - 화이트, 아이보리, 크림, 라이트 그레이를 사용한다.
                - 색상보다 형태와 공간의 여백이 먼저 느껴지도록 한다.
        
                공간/소재:
                - 장식이 없는 무지 벽면
                - 원목 또는 리넨 텍스처
                - 자연스럽고 절제된 소재
                - 비어 있는 넓은 공간
                - 최소한의 건축 요소
                - 하나의 오브제만 강조하는 공간 구성
                - 넓고 깨끗한 벽면을 적극적으로 활용한다.
        
                조명:
                - 자연광 중심의 은은한 확산광을 사용한다.
                - 그림자는 옅고 부드럽게 표현한다.
                - 빛이 공간 전체에 자연스럽게 퍼지는 느낌을 준다.
                - 강한 스포트라이트나 극적인 명암 대비를 피한다.
                - 오브제의 형태가 부드러운 빛으로 자연스럽게 드러나도록 한다.
        
                구도:
                - 오브제 하나를 중심으로 구성한다.
                - 충분한 네거티브 스페이스를 확보한다.
                - 프레임 가장자리를 비워둔다.
                - 넓은 빈 공간을 적극적으로 활용한다.
                - 오브제는 중앙 또는 중앙에서 약간 벗어난 위치에 배치할 수 있다.
                - 비어 있는 공간 자체가 디자인 요소가 되도록 구성한다.
        
        
                ============================================================
                [REFERENCE IMAGE STYLE]
                ============================================================
        
                제공된 무드별 레퍼런스 이미지의 전체적인 공간 스타일을 참고한다.
        
                레퍼런스 이미지처럼
                럭셔리 패션 브랜드의 공간과 현대 건축,
                조형적인 오브제, 소재의 질감, 빛과 그림자를 결합한다.
        
                특히 다음 특징을 공통적인 시각 언어로 사용한다.
        
                - 건축 공간이 이미지의 주인공
                - 크고 단순한 기하학적 건축 구조
                - 소수의 조형적인 오브제
                - 트래버틴, 스톤, 콘크리트, 우드, 메탈, 가죽 등의 고급 소재
                - 소재의 미세한 질감이 실제처럼 보이는 사실적인 표현
                - 벽면을 따라 흐르는 간접조명
                - 공간의 깊이를 만드는 그림자
                - 바닥에 자연스럽게 반사되는 빛
                - 과도한 장식이 없는 절제된 구성
                - 넓은 네거티브 스페이스
                - 시네마틱하고 고급스러운 공간감
        
                레퍼런스 이미지의 구성을 그대로 복제하지 않는다.
                레퍼런스의 분위기, 공간 언어, 소재감, 조명 방식만 참고하여
                새로운 MCM 브랜드 아트워크를 생성한다.
        
        
                ============================================================
                [MCM BRAND AESTHETIC]
                ============================================================
        
                MCM 특유의 프리미엄 가죽 질감과 현대적인 럭셔리 패션 브랜드의 감성을
                건축 공간과 조형적인 오브제로 표현한다.
        
                고급스러운 가죽 질감,
                정교한 금속 요소,
                석재,
                브론즈,
                우드,
                콘크리트 등의 소재를 무드에 맞게 선택한다.
        
                MCM 비세토스 모노그램에서 영감을 받은 패턴이나 텍스처는
                전체 이미지에 과도하게 반복하지 않는다.
        
                모노그램을 직접적인 그래픽 패턴으로 크게 표시하지 않고,
                일부 오브제 또는 소재 표면에 매우 은은하게 반영한다.
        
                브랜드 로고와 브랜드명을 직접적으로 크게 표시하지 않는다.
        
                브랜드 정체성은 로고보다
                소재, 형태, 공간, 조명, 고급스러운 디테일을 통해 느껴지도록 한다.
        
        
                ============================================================
                [PRODUCT-INSPIRED ART OBJECT]
                ============================================================
        
                주요 제품 카테고리 '%s'에서 영감을 받은
                하나 또는 소수의 조형적인 3D 아트 오브제를 공간에 배치한다.
        
                실제 제품을 그대로 재현하지 않는다.
        
                제품의:
                - 실루엣
                - 구조
                - 형태
                - 비례
                - 곡선
                - 가죽 질감
                - 금속 하드웨어의 기하학적 인상
        
                등을 추상적으로 차용한다.
        
                제품이 실제 상품처럼 보이지 않도록 한다.
        
                "제품을 전시하는 공간"이 아니라
                "제품에서 영감을 받은 조형물이 존재하는 건축 공간"처럼 표현한다.
        
                제품 오브제는 전체 공간과 자연스럽게 어우러져야 하며
                이미지 전체를 압도하지 않는다.
        
        
                ============================================================
                [CUSTOMER INFORMATION]
                ============================================================
        
                쇼핑 목적 '%s'와 고객 성별 '%s'는
                선택된 무드의 기본적인 비주얼 규칙을 변경하지 않는다.
        
                고객 정보는 선택된 무드 안에서
                소재, 오브제의 형태, 공간의 디테일을 미세하게 조정하는
                보조 조건으로만 사용한다.
        
                최우선 기준은 반드시 오늘의 무드 '%s'이다.
        
                방문 시간 '%s'는 시간대에 맞게
                자연광, 인공조명, 색온도, 야간 분위기 등을
                자연스럽게 조정하는 데 활용한다.
        
        
                ============================================================
                [DIGITAL ART WALL LAYOUT]
                ============================================================
        
                실제 매장의 대형 가로형 디지털 아트월을 기준으로 구성한다.
        
                웅장하고 시네마틱한 가로형 공간을 표현한다.
        
                화면 중앙 또는 향후 문구와 고객 맞춤 UI가 배치될 영역에는
                충분한 네거티브 스페이스를 확보한다.
        
                중앙부에 복잡한 오브제나 작은 디테일을 과도하게 배치하지 않는다.
        
                향후 디지털 스크린 위에 텍스트와 제품 UI가 추가될 예정이므로
                텍스트 영역을 침범하는 복잡한 패턴을 사용하지 않는다.
        
                화면 전체가 하나의 연결된 건축 공간처럼 보여야 한다.
        
                이미지 분할, 콜라주, 여러 장면의 병렬 배치를 사용하지 않는다.
        
        
                ============================================================
                [VISUAL QUALITY]
                ============================================================
        
                실제 럭셔리 패션 브랜드 플래그십 스토어의
                대형 디지털 아트월에 사용할 수 있는 수준의
                고해상도 프리미엄 비주얼을 생성한다.
        
                사진처럼 사실적인 소재 표현.
                정교한 건축 디테일.
                실제 석재와 콘크리트의 미세한 표면 질감.
                고급 가죽과 메탈의 사실적인 재질.
                자연스러운 빛의 반사.
                깊이 있는 그림자.
                시네마틱한 공간감.
                정교한 3D architectural visualization.
        
                과도한 CGI 느낌을 피한다.
                플라스틱처럼 지나치게 매끈한 표면을 피한다.
                실제 건축 사진처럼 자연스러운 재질의 미세한 불완전함을 유지한다.
        
                전체적으로 조용하고 절제되어 있지만
                한눈에 보았을 때 럭셔리함과 강한 공간적 인상이 느껴지는
                프리미엄 패션 아트워크를 생성한다.
        
                사람 없음.
                모델 없음.
                쇼핑객 없음.
                제품 광고 느낌 없음.
                큰 로고 없음.
                브랜드명 없음.
                텍스트 없음.
                워터마크 없음.
                과도한 패턴 없음.
                과도한 소품 없음.
                하나의 완성된 건축 공간으로 표현한다.
                """,

                storeName,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null ? visitTime.getHour() + "시" : "현재 시간대",

                // Mood priority
                moodCategoryStr,
                aimood,

                // Product
                productTheme,

                // Customer information
                purposeText,
                genderStr,
                moodCategoryStr,
                visitTime != null ? visitTime.getHour() + "시" : "현재 시간대"
        );

        // 이미지 생성 API 호출 (생성된 이미지 URL을 반환받음)
        String base64Image = generateImageOpenAI(prompt);
        String localSavedPath = saveBase64Image(base64Image, visitCard.getVisitCardId());

        String prompt2 = String.format(
                """
         MCM 럭셔리 패션 브랜드 매장 '%s'의 고객 맞춤형 디지털 아트월에 배치할
         영문 문구 1개를 추천해줘.
 
         고객의 오늘의 무드는 '%s'이다.
         AI가 생성하는 모든 문구는 반드시 해당 무드의 생성 기준, 문장 구조 규칙,
         핵심 어휘를 따라야 한다.
 
         [고객 정보]
         - 오늘의 무드: %s
         - AI 무드: %s
         - 고객 성별: %s
         - 주요 제품 카테고리: %s
         - 쇼핑 목적: %s
         - 방문 시간: %s
 
         [무드별 문구 생성 기준]
 
         1. 스트리트 (Street)
 
         생성 기준(톤앤매너):
         - 대담하고 즉흥적인 에너지
         - 도시와 움직임의 이미지
         - 자유롭고 자신감 있는 분위기
 
         문장 구조 규칙:
         - 짧은 명령형 또는 선언형 문장 2개를 사용한다.
         - 두 문장을 마침표로 끊어 리듬감을 준다.
         - 영문 기준 12단어 이내로 작성한다.
 
         핵심 어휘:
         Move, Bold, Rule, Own, Street, Fear Less
 
         예시 문구:
         "Move Bold. Own Your Journey."
         "Rule the Street, Own the Moment."
         "Fear Less, Move More."
 
 
         2. 클래식 (Classic)
 
         생성 기준(톤앤매너):
         - 우아하고 시간을 초월한 정서
         - 이야기와 유산의 은유
         - 품격 있고 세련된 분위기
 
         문장 구조 규칙:
         - 완결된 서술형 한 문장으로 작성한다.
         - 부드럽고 자연스럽게 흐르는 구조를 사용한다.
 
         핵심 어휘:
         Story, Timeless, Carry, Legacy, Grace
 
         예시 문구:
         "A Story Worth Carrying."
         "Timeless, Just Like You."
         "Elegance Never Fades."
 
 
         3. 모던 (Modern)
 
         생성 기준(톤앤매너):
         - 절제되고 미니멀한 감각
         - 군더더기 없는 대비
         - 간결하고 세련된 분위기
 
         문장 구조 규칙:
         - 짧은 대구 구조를 사용한다.
         - A, B 형태로 구성한다.
         - 쉼표를 사용하여 두 요소의 대비를 강조한다.
 
         핵심 어휘:
         Simple, Clean, Clear, Less, Structured
 
         예시 문구:
         "Simplicity Speaks Loudest."
         "Less Noise, More You."
         "Clean Lines, Clear Mind."
 
 
         4. 볼드 (Bold)
 
         생성 기준(톤앤매너):
         - 강렬한 확신
         - 자기표현의 임팩트
         - 자신감 있고 대담한 분위기
 
         문장 구조 규칙:
         - 짧고 강한 단언형 문장을 사용한다.
         - 단어 수를 절제하여 임팩트를 극대화한다.
 
         핵심 어휘:
         Unapologetic, Bold, Own, Statement, Loud
 
         예시 문구:
         "Unapologetically You."
         "Bold Moves Only."
         "Make Them Look Twice."
 
 
         5. 미니멀 (Minimal)
 
         생성 기준(톤앤매너):
         - 여백과 본질에 집중하는 조용한 정서
         - 꾸밈을 덜어낸 상태 그 자체를 미학으로 제시한다.
         - 절제되고 차분한 분위기를 유지한다.
 
         문장 구조 규칙:
         - 짧은 명사형 또는 단문 종결을 사용한다.
         - 수식어를 최소화한다.
         - 하나의 짧은 문장으로 작성한다.
         - 영문 기준 6단어 이내를 권장한다.
 
         핵심 어휘:
         Essence, Quiet, Bare, Nothing, Enough
 
         예시 문구:
         "Nothing Extra."
         "Just Enough."
         "The Essence Remains."
 
 
         [쇼핑 목적 반영 규칙]
 
         쇼핑 목적은 문구의 기본 무드를 변경하지 않는 범위에서만 보조적으로 반영한다.
 
         특히 쇼핑 목적이 '선물'인 경우,
         전하다, 간직하다, 특별한 순간과 같은 의미를 자연스럽게 반영한다.
 
         단, 쇼핑 목적 때문에 해당 무드의 문장 구조와 톤앤매너가 변경되어서는 안 된다.
 
 
         [제품 카테고리 반영 규칙]
 
         주요 제품 카테고리는 문구의 기본 무드를 변경하지 않는 범위에서만 보조적으로 반영한다.
 
         제품명을 단순히 반복하거나 직접적인 상품 광고 문구를 만들지 않는다.
         제품의 실루엣, 스타일, 움직임, 소유의 의미 등을
         문구에 자연스럽게 연상시키는 수준으로 반영한다.
 
 
         [MCM 브랜드 톤]
 
         MCM의 럭셔리 패션 브랜드 이미지와 어울리는
         세련되고 현대적인 캠페인 카피처럼 작성한다.
 
         지나치게 상업적이거나 일반적인 광고 문구처럼 작성하지 않는다.
         디지털 아트월에 배치했을 때 시각적으로 아름답고
         짧은 문장만으로도 분위기를 전달할 수 있어야 한다.
 
 
         [매우 중요한 생성 규칙]
 
         - 반드시 '%s' 무드의 규칙만 적용한다.
         - 다른 무드의 문장 구조나 분위기를 섞지 않는다.
         - 정확히 1개의 문구를 생성한다.
         - 모든 문구는 영어로 작성한다.
         - 문법적으로 자연스러운 영어를 사용한다.
         - 핵심 어휘를 적절히 활용하되 모든 문구에 동일한 단어를 반복하지 않는다.
         - 예시 문구를 그대로 복사하지 않고 새로운 문구를 생성한다.
         - 문구 앞뒤에 따옴표를 붙이지 않는다.
         - 번호, 설명, 해설, 불릿을 추가하지 않는다.
         - 반드시 JSON 배열 하나만 반환한다.
 
         반환 형식:
         ["문구1"]
         """,
                storeName,
                moodCategoryStr,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null
                        ? visitTime.getHour() + "시"
                        : "현재 시간대",
                moodCategoryStr
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
        MCM 럭셔리 패션 브랜드 매장 '%s'의 고객 맞춤형 디지털 아트월에 배치할
        영문 문구 5개를 추천해줘.

        고객의 오늘의 무드는 '%s'이다.
        AI가 생성하는 모든 문구는 반드시 해당 무드의 생성 기준, 문장 구조 규칙,
        핵심 어휘를 따라야 한다.

        [고객 정보]
        - 오늘의 무드: %s
        - AI 무드: %s
        - 고객 성별: %s
        - 주요 제품 카테고리: %s
        - 쇼핑 목적: %s
        - 방문 시간: %s

        [무드별 문구 생성 기준]

        1. 스트리트 (Street)

        생성 기준(톤앤매너):
        - 대담하고 즉흥적인 에너지
        - 도시와 움직임의 이미지
        - 자유롭고 자신감 있는 분위기

        문장 구조 규칙:
        - 짧은 명령형 또는 선언형 문장 2개를 사용한다.
        - 두 문장을 마침표로 끊어 리듬감을 준다.
        - 영문 기준 12단어 이내로 작성한다.

        핵심 어휘:
        Move, Bold, Rule, Own, Street, Fear Less

        예시 문구:
        "Move Bold. Own Your Journey."
        "Rule the Street, Own the Moment."
        "Fear Less, Move More."


        2. 클래식 (Classic)

        생성 기준(톤앤매너):
        - 우아하고 시간을 초월한 정서
        - 이야기와 유산의 은유
        - 품격 있고 세련된 분위기

        문장 구조 규칙:
        - 완결된 서술형 한 문장으로 작성한다.
        - 부드럽고 자연스럽게 흐르는 구조를 사용한다.

        핵심 어휘:
        Story, Timeless, Carry, Legacy, Grace

        예시 문구:
        "A Story Worth Carrying."
        "Timeless, Just Like You."
        "Elegance Never Fades."


        3. 모던 (Modern)

        생성 기준(톤앤매너):
        - 절제되고 미니멀한 감각
        - 군더더기 없는 대비
        - 간결하고 세련된 분위기

        문장 구조 규칙:
        - 짧은 대구 구조를 사용한다.
        - A, B 형태로 구성한다.
        - 쉼표를 사용하여 두 요소의 대비를 강조한다.

        핵심 어휘:
        Simple, Clean, Clear, Less, Structured

        예시 문구:
        "Simplicity Speaks Loudest."
        "Less Noise, More You."
        "Clean Lines, Clear Mind."


        4. 볼드 (Bold)

        생성 기준(톤앤매너):
        - 강렬한 확신
        - 자기표현의 임팩트
        - 자신감 있고 대담한 분위기

        문장 구조 규칙:
        - 짧고 강한 단언형 문장을 사용한다.
        - 단어 수를 절제하여 임팩트를 극대화한다.

        핵심 어휘:
        Unapologetic, Bold, Own, Statement, Loud

        예시 문구:
        "Unapologetically You."
        "Bold Moves Only."
        "Make Them Look Twice."


        5. 미니멀 (Minimal)

        생성 기준(톤앤매너):
        - 여백과 본질에 집중하는 조용한 정서
        - 꾸밈을 덜어낸 상태 그 자체를 미학으로 제시한다.
        - 절제되고 차분한 분위기를 유지한다.

        문장 구조 규칙:
        - 짧은 명사형 또는 단문 종결을 사용한다.
        - 수식어를 최소화한다.
        - 하나의 짧은 문장으로 작성한다.
        - 영문 기준 6단어 이내를 권장한다.

        핵심 어휘:
        Essence, Quiet, Bare, Nothing, Enough

        예시 문구:
        "Nothing Extra."
        "Just Enough."
        "The Essence Remains."


        [쇼핑 목적 반영 규칙]

        쇼핑 목적은 문구의 기본 무드를 변경하지 않는 범위에서만 보조적으로 반영한다.

        특히 쇼핑 목적이 '선물'인 경우,
        전하다, 간직하다, 특별한 순간과 같은 의미를 자연스럽게 반영한다.

        단, 쇼핑 목적 때문에 해당 무드의 문장 구조와 톤앤매너가 변경되어서는 안 된다.


        [제품 카테고리 반영 규칙]

        주요 제품 카테고리는 문구의 기본 무드를 변경하지 않는 범위에서만 보조적으로 반영한다.

        제품명을 단순히 반복하거나 직접적인 상품 광고 문구를 만들지 않는다.
        제품의 실루엣, 스타일, 움직임, 소유의 의미 등을
        문구에 자연스럽게 연상시키는 수준으로 반영한다.


        [MCM 브랜드 톤]

        MCM의 럭셔리 패션 브랜드 이미지와 어울리는
        세련되고 현대적인 캠페인 카피처럼 작성한다.

        지나치게 상업적이거나 일반적인 광고 문구처럼 작성하지 않는다.
        디지털 아트월에 배치했을 때 시각적으로 아름답고
        짧은 문장만으로도 분위기를 전달할 수 있어야 한다.


        [매우 중요한 생성 규칙]

        - 반드시 '%s' 무드의 규칙만 적용한다.
        - 다른 무드의 문장 구조나 분위기를 섞지 않는다.
        - 정확히 5개의 서로 다른 문구를 생성한다.
        - 모든 문구는 영어로 작성한다.
        - 문법적으로 자연스러운 영어를 사용한다.
        - 핵심 어휘를 적절히 활용하되 모든 문구에 동일한 단어를 반복하지 않는다.
        - 예시 문구를 그대로 복사하지 않고 새로운 문구를 생성한다.
        - 문구 앞뒤에 따옴표를 붙이지 않는다.
        - 번호, 설명, 해설, 불릿을 추가하지 않는다.
        - 반드시 JSON 배열 하나만 반환한다.

        반환 형식:
        ["문구1", "문구2", "문구3", "문구4", "문구5"]
        """,
                storeName,
                moodCategoryStr,
                moodCategoryStr,
                aimood,
                genderStr,
                productTheme,
                purposeText,
                visitTime != null
                        ? visitTime.getHour() + "시"
                        : "현재 시간대",
                moodCategoryStr
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
            // 1. null 또는 빈 값 체크
            if (base64Image == null || base64Image.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 이미지 데이터가 비어있습니다.");
            }

            // 2. Base64 Prefix(data:image/png;base64,) 제거
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            // 3. 줄바꿈 및 공백 문자 제거
            base64Image = base64Image.replaceAll("\\s+", "");

            // 4. 업로드 디렉터리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 5. 파일 생성 및 저장
            String fileName = visitCardId + ".png";
            Path targetPath = uploadPath.resolve(fileName);

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            Files.write(targetPath, imageBytes);

            System.out.println("이미지 저장 완료: " + targetPath.toAbsolutePath());

            // 6. DB에 저장할 웹 접속 URL 또는 파일 경로 반환
            // UPLOAD_DIR 끝에 이미 '/'가 들어가 있으므로 그대로 결합 가능
            return UPLOAD_DIR + fileName;

        } catch (Exception e) {
            System.out.println("Base64 저장 실패: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 상세한 에러 스택트레이스 출력
            throw new RuntimeException("Base64 이미지를 로컬에 저장하는 중 오류가 발생했습니다.", e);        }
    }
}