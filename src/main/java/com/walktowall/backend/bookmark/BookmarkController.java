package com.walktowall.backend.bookmark;

import com.walktowall.backend.bookmark.dto.BookmarkListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/wishlist")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<BookmarkListResponse> readBookmark() {
        Integer userId = 1; // 1번 유저로 고정
        return ResponseEntity.ok(bookmarkService.readBookmarks(userId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<String> createBookmark(@PathVariable Integer productId) {
        Integer userId = 1; // 1번 유저로 고정
        bookmarkService.createBookmark(userId, productId);
        return ResponseEntity.ok("위시 상품으로 등록되었습니다.");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteBookmark(@PathVariable Integer productId) {
        Integer userId = 1; // 1번 유저로 고정
        bookmarkService.deleteBookmark(userId, productId);
        return ResponseEntity.ok("위시 상품이 삭제되었습니다.");
    }
}
