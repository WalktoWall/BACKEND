package com.walktowall.backend.bookmark;

import com.walktowall.backend.bookmark.dto.BookmarkListResponse;
import com.walktowall.backend.bookmark.dto.CreateBookmarkResponse;
import com.walktowall.backend.bookmark.dto.DeleteBookmarkResponse;
import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.repository.ProductRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CreateBookmarkResponse createBookmark(Integer userId, Integer productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (bookmarkRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            // 409 Conflict 예외 발생
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 북마크한 상품입니다.");
        }


        BookmarkEntity bookmark = BookmarkEntity.builder()
                .user(user)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);

        return CreateBookmarkResponse.builder()
                .message("위시 상품으로 등록되었습니다.")
                .build();
    }

    @Transactional
    public DeleteBookmarkResponse deleteBookmark(Integer userId, Integer productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        // 삭제 전 북마크 존재 여부 검증 (없으면 404 예외)
        if (!bookmarkRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "위시리스트에 해당 상품이 존재하지 않습니다.");
        }

        bookmarkRepository.deleteByUser_UserIdAndProduct_ProductId(userId, productId);

        return DeleteBookmarkResponse.builder()
                .message("위시 상품이 삭제되었습니다.")
                .build();
    }

    public BookmarkListResponse readBookmarks(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<BookmarkEntity> bookmarkEntityList = bookmarkRepository.findByUser_UserId(userId);

        List<BookmarkListResponse.Product> productList = bookmarkEntityList.stream()
                .map(bookmark -> BookmarkListResponse.Product.builder()
                        .productId(bookmark.getProduct().getProductId())
                        .productName(bookmark.getProduct().getProductName())
                        .build())
                .toList();

        return BookmarkListResponse.builder()
                .message("위시리스트 조회를 성공적으로 완료했습니다.")
                .productList(productList)
                .build();
    }
}
