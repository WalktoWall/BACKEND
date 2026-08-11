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

    //매장 목록 조회
    public List<OfflineStoreResponse> getOfflineStores() {

        List<OfflineStore> offlineStores = offlineStoreRepository.findAll();

        return offlineStores.stream()
                .map(this::toResponse)
                .toList();
    }

    private OfflineStoreResponse toResponse(OfflineStore offlineStore) {

        return OfflineStoreResponse.builder()
                .storeId(offlineStore.getStoreId())
                .regionCategory(offlineStore.getRegionCategory())
                .storeName(offlineStore.getStoreName())
                .build();
    }
}
