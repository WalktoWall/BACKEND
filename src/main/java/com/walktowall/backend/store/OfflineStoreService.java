package com.walktowall.backend.store;

import com.walktowall.backend.store.dto.OfflineStoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfflineStoreService {
    private final OfflineStoreRepository offlineStoreRepository;

    // 매장 목록 조회
    public List<OfflineStoreResponse> getOfflineStores(Integer regionCategory) {

        List<OfflineStore> offlineStores;

        // 지역 선택 안 함 → 전체 매장 조회
        if (regionCategory == null) {
            offlineStores = offlineStoreRepository.findAll();
        }
        // 지역 선택함 → 해당 지역 매장만 조회
        else {
            offlineStores = offlineStoreRepository.findByRegionCategory(regionCategory);
        }

        return offlineStores.stream()
                .map(this::toResponse)
                .toList();
    }

    private OfflineStoreResponse toResponse(OfflineStore offlineStore) {

        return OfflineStoreResponse.builder()
                .storeId(offlineStore.getStoreId())
                .regionCategory(offlineStore.getRegionCategory())
                .storeName(offlineStore.getStoreName())
                .openTime(offlineStore.getOpenTime())
                .closeTime(offlineStore.getCloseTime())
                .build();
    }
}
