package com.walktowall.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Long userId(Integer userId);

    Optional<User> findByUserId(Integer userId);
}
