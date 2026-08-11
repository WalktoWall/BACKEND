package com.walktowall.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Integer> {
    boolean existsByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
    void deleteByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
}
