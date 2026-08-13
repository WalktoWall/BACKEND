package com.walktowall.backend.bookmark;

import com.walktowall.backend.product.entity.ProductEntity;
import com.walktowall.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist") // 테이블명은 erd와 동일하게 wishlist로 지정
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BookmarkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Integer bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
}