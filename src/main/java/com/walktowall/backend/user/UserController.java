package com.walktowall.backend.user;

import com.walktowall.backend.user.dto.MyPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    // 마이페이지
    @GetMapping("/me")
    public ResponseEntity<MyPageResponse> getMyPage() {
        Integer userId = 1; // 시연 시 userId는 1로 고정

        MyPageResponse response = userService.getMyPage(userId);

        return ResponseEntity.ok(response);
    }
}
