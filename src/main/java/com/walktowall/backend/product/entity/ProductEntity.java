package com.walktowall.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_category", nullable = false)
    private Integer productCategory = 1; // 1="백팩", 2="토트백", 3="지갑", 4="악세서리"
    // derault는 1

    @Column(name = "product_img", nullable = false)
    private String productImg;

    @Column(name = "product_detail", nullable = false)
    private String productDetail;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "location", nullable = false)
    private String location;
}
