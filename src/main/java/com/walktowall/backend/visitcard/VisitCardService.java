package com.walktowall.backend.visitcard;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.store.OfflineStoreRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import com.walktowall.backend.visitcard.dto.VisitCardCreateRequest;
import com.walktowall.backend.visitcard.dto.VisitCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitCardService {
    private final VisitCardRepository visitCardRepository;
    private final UserRepository userRepository;
    private final OfflineStoreRepository offlineStoreRepository;

    //visit card 생성
    @Transactional
    public VisitCardResponse createVisitCard(VisitCardCreateRequest request) {

        // 로그인 기능이 없으므로 고정 사용자 1번을 사용
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 선택한 매장 조회
        OfflineStore offlineStore = offlineStoreRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        VisitCard visitCard = new VisitCard();

        visitCard.setUser(user);
        visitCard.setOfflineStore(offlineStore);
        visitCard.setFindProductCategory(request.getFindProductCategory());
        visitCard.setMoodCategory(request.getMoodCategory());
        visitCard.setPurposeText(request.getPurposeText());
        visitCard.setVisitTime(request.getVisitTime());
        visitCard.setSupportStatus(request.getSupportStatus());

        // 실제 AI 연동 전까지는 빈 값으로 저장
        visitCard.setAiMood(null);

        VisitCard savedVisitCard = visitCardRepository.save(visitCard);

        return toResponse(savedVisitCard);
    }

    //visit card 단건 조회
    public VisitCardResponse getVisitCard(Integer visitCardId) {

        VisitCard visitCard = visitCardRepository.findById(visitCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Visit Card입니다."));

        return toResponse(visitCard);
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


