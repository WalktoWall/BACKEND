package com.walktowall.backend.store.history;

import com.walktowall.backend.store.history.dto.OfflineHistoryResponse;
import com.walktowall.backend.visitcard.VisitCard;
import com.walktowall.backend.visitcard.VisitCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OfflineHistoryService {

    private final OfflineHistoryRepository offlineHistoryRepository;
    private final VisitCardRepository visitCardRepository;

    // 매장 입장 기록 생성
    public OfflineHistoryResponse enterStore(
            Integer visitCardId,
            Integer qrStoreId
    ) {
        VisitCard visitCard = visitCardRepository.findById(visitCardId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 Visit Card입니다."
                        )
                );

        // VisitCard가 선택한 매장
        Integer visitCardStoreId =
                visitCard.getOfflineStore().getStoreId();

        // QR 매장과 VisitCard 매장 비교
        if (!visitCardStoreId.equals(qrStoreId)) {
            throw new IllegalArgumentException(
                    "Visit Card의 매장과 QR의 매장이 일치하지 않습니다."
            );
        }

        // 매장이 일치하면 방문 기록 생성
        OfflineHistory history = new OfflineHistory();

        history.setVisitCard(visitCard);
        history.setEnterTime(LocalDateTime.now());

        OfflineHistory savedHistory =
                offlineHistoryRepository.save(history);

        return toResponse(savedHistory);
    }

    // 매장 퇴장 기록
    public OfflineHistoryResponse leaveStore(Integer visitCardId) {

        OfflineHistory history =
                offlineHistoryRepository
                        .findByVisitCardVisitCardId(visitCardId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하는 매장 방문 기록이 없습니다."
                                ));

        if (history.getLeaveTime() != null) {
            throw new IllegalStateException("이미 퇴장 처리된 방문 기록입니다.");
        }

        history.setLeaveTime(LocalDateTime.now());

        return toResponse(history);
    }

    private OfflineHistoryResponse toResponse(OfflineHistory history) {

        return OfflineHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .visitCardId(
                        history.getVisitCard().getVisitCardId()
                )
                .storeId(
                        history.getVisitCard()
                                .getOfflineStore()
                                .getStoreId()
                )
                .enterTime(history.getEnterTime())
                .leaveTime(history.getLeaveTime())
                .build();
    }
}