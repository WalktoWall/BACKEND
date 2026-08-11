package com.walktowall.backend.bookmark;

import com.walktowall.backend.bookmark.dto.BookmarkListResponse;
import com.walktowall.backend.product.dto.ProductDetailResponse;
import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.repository.ProductRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void createBookmark(Integer userId, Integer productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        if (bookmarkRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            throw new RuntimeException("이미 북마크한 상품입니다.");
        }

        BookmarkEntity bookmark = BookmarkEntity.builder()
                .user(user)
                .product(product)
                .build();

        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Integer userId, Integer productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        bookmarkRepository.deleteByUser_UserIdAndProduct_ProductId(userId, productId);
    }

    public BookmarkListResponse getMyBookmarks(Integer userId) {
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
                .productList(productList) // 타입 일치 (List<BookmarkListResponse.Product>)
                .build();
    }
}
