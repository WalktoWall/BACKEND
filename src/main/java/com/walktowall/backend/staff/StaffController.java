package com.walktowall.backend.staff;

import com.walktowall.backend.staff.dto.StaffCustomerResponse;
import com.walktowall.backend.staff.dto.StaffVisitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    //오늘 방문 예정 고객 목록 조회
    @GetMapping("/visits/today")
    public ResponseEntity<List<StaffCustomerResponse>> getTodayCustomers() {

        List<StaffCustomerResponse> response =
                staffService.getTodayCustomers();

        return ResponseEntity.ok(response);
    }

    // 직원용 고객 Visit Card 상세 조회 (동선, 저장된 추천 상품, AI 가이드 포함)
    @GetMapping("/visits/{visitCardId}")
    public ResponseEntity<StaffVisitResponse> getVisitDetail(
            @PathVariable Integer visitCardId
    ) {

        StaffVisitResponse response =
                staffService.getVisitDetail(visitCardId);

        return ResponseEntity.ok(response);
    }

}