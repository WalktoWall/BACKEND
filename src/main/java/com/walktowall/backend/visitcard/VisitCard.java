package com.walktowall.backend.visitcard;

import com.walktowall.backend.store.OfflineStore;
import com.walktowall.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "visit_cards")
public class VisitCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_card_id")
    private Integer visitCardId;

    // visit card를 작성한 user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // user가 선택한 방문 매장
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private OfflineStore offlineStore;

    // 추가된 성별 필드
    @Column(name = "gender", nullable = false)
    private Integer gender;

    // 찾는 상품 카테고리
    @Column(name = "find_product_category", nullable = true)
    private Integer findProductCategory;

    // 오늘의 무드 카테고리
    @Column(name = "mood_category", nullable = false)
    private Integer moodCategory;

    // 방문 목적 또는 자유 입력 내용
    @Column(name = "purpose_text", nullable = false)
    private String purposeText;

    // 방문 예정 시간
    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;

    // 직원 응대 희망 여부/상태
    @Column(name = "support_status", nullable = false)
    private Integer supportStatus;

    // AI 한 줄 요약: 실제 AI 연동 전이라 비워둠
    @Column(name = "ai_mood")
    private String aiMood;

    @Column(name = "recommended_route")
    private String recommendedRoute; // DB 저장용 (예: "여성존 -> 신상품존 -> 라이프스타일존")

    // Visit Card 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
