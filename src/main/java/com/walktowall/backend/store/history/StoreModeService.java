package com.walktowall.backend.store.history;

import com.walktowall.backend.store.history.dto.OfflineHistoryResponse;
import com.walktowall.backend.store.history.dto.StoreModeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreModeService {

    private final OfflineHistoryService offlineHistoryService;

    // 매장 모드 ON
    public StoreModeResponse enterStore(
            Integer visitCardId,
            Integer qrStoreId
    ) {

        OfflineHistoryResponse history =
                offlineHistoryService.enterStore(
                        visitCardId,
                        qrStoreId
                );

        return StoreModeResponse.builder()
                .storeMode(true)
                .storeId(history.getStoreId())
                .visitCardId(history.getVisitCardId())
                .history(history)
                .build();
    }

    // 매장 모드 OFF
    public StoreModeResponse leaveStore(Integer visitCardId) {

        OfflineHistoryResponse history =
                offlineHistoryService.leaveStore(visitCardId);

        return StoreModeResponse.builder()
                .storeMode(false)
                .storeId(history.getStoreId())
                .visitCardId(history.getVisitCardId())
                .history(history)
                .build();
    }
}