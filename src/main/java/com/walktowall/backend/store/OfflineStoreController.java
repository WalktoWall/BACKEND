package com.walktowall.backend.store;

import com.walktowall.backend.store.dto.OfflineStoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class OfflineStoreController {
    private final OfflineStoreService offlineStoreService;

    //오프라인 매장 목록 조회
    @GetMapping
    public ResponseEntity<List<OfflineStoreResponse>> getOfflineStores(
            @RequestParam(required = false) Integer regionCategory) {

        List<OfflineStoreResponse> response = offlineStoreService.getOfflineStores(regionCategory);
        return ResponseEntity.ok(response);
    }
}
