package com.walktowall.backend.store.history;

import com.walktowall.backend.store.history.dto.StoreModeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.walktowall.backend.store.history.dto.StoreModeRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store-mode")
public class StoreModeController {

    private final StoreModeService storeModeService;

    //매장 모드 ON
    @PostMapping("/{visitCardId}")
    public ResponseEntity<StoreModeResponse> enterStore(
            @PathVariable Integer visitCardId,
            @RequestBody StoreModeRequest request
    ) {

        StoreModeResponse response =
                storeModeService.enterStore(
                        visitCardId,
                        request.getStoreId()
                );

        return ResponseEntity.ok(response);
    }

    // 매장 모드 OFF
    @PostMapping ("/{visitCardId}/leave")
    public ResponseEntity<StoreModeResponse> leaveStore(
            @PathVariable Integer visitCardId
    ) {

        StoreModeResponse response =
                storeModeService.leaveStore(visitCardId);

        return ResponseEntity.ok(response);
    }
}