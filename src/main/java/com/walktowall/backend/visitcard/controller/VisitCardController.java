package com.walktowall.backend.visitcard;

import com.walktowall.backend.visitcard.dto.VisitCardCreateRequest;
import com.walktowall.backend.visitcard.dto.VisitCardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visitcards")

public class VisitCardController {
    private final VisitCardService visitCardService;

    //Visit Card 생성
    @PostMapping
    public ResponseEntity<VisitCardResponse> createVisitCard(
            @Valid @RequestBody VisitCardCreateRequest request
    ) {
        VisitCardResponse response = visitCardService.createVisitCard(request);

        return ResponseEntity.status(201).body(response);
    }

    // Visit Card 단건 조회
    @GetMapping("/{visitCardId}")
    public ResponseEntity<VisitCardResponse> getVisitCard(
            @PathVariable Integer visitCardId
    ) {
        VisitCardResponse response = visitCardService.getVisitCard(visitCardId);

        return ResponseEntity.ok(response);
    }
}
