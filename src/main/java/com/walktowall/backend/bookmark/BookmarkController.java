package com.walktowall.backend.bookmark;

import com.walktowall.backend.bookmark.dto.BookmarkListResponse;
import com.walktowall.backend.bookmark.dto.CreateBookmarkResponse;
import com.walktowall.backend.bookmark.dto.DeleteBookmarkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CreateBookmarkResponse> createBookmark(@PathVariable Integer productId) {
        Integer userId = 1; // 1번 유저로 고정
        CreateBookmarkResponse response = bookmarkService.createBookmark(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<DeleteBookmarkResponse> deleteBookmark(@PathVariable Integer productId) {
        Integer userId = 1; // 1번 유저로 고정
        DeleteBookmarkResponse response = bookmarkService.deleteBookmark(userId, productId);
        return ResponseEntity.ok(response);
    }
}
