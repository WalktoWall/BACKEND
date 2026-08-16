package com.walktowall.backend.visitcard;

import com.walktowall.backend.visitcard.dto.RecommendProductResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recommended_products")
public class RecommendedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommended_product_id")
    private Long id;

    // 어떤 VisitCard에 대한 추천 상품인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_card_id", nullable = false)
    private VisitCard visitCard;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_img")
    private String productImg;

    @Column(name = "product_zone")
    private String productZone;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_detail", length = 1000)
    private String productDetail;

    /**
     * DTO와 VisitCard 엔티티를 받아 RecommendedProduct 엔티티로 변환하는 팩토리 메서드
     */
    public static RecommendedProduct from(RecommendProductResponse.ProductDto dto, VisitCard visitCard) {
        return RecommendedProduct.builder()
                .visitCard(visitCard)
                .productId(dto.getProductId())
                .productImg(dto.getProductImg())
                .productZone(dto.getZone())
                .productName(dto.getProductName())
                .productDetail(dto.getProductDetail())
                .build();
    }
}