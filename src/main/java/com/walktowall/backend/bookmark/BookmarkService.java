package com.walktowall.backend.bookmark;

import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.product.repository.ProductRepository;
import com.walktowall.backend.user.User;
import com.walktowall.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
