package com.walktowall.backend.product.entity;

import com.walktowall.backend.visitcard.VisitCard;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name="product_scan")
public class ProductScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_scan_id")
    private Integer productScanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_card_id", nullable = false)
    private VisitCard visitCard;
}
