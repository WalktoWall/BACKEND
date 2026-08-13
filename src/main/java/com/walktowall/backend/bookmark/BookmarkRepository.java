package com.walktowall.backend.bookmark;

import com.walktowall.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Integer> {
    boolean existsByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);
    void deleteByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);

    List<BookmarkEntity> findByUser_UserId(Integer userId);
}
