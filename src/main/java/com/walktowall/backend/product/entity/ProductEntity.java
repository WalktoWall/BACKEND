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

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_category")
    private Integer productCategory; // 1="백팩", 2="토트백", 3="지갑", 4="악세서리"

    @Column(name = "product_img")
    private String productImg;

    @Column(name = "product_detail") // ERD의 대표 메뉴(여러 메뉴를 하나의 문자열로 저장)
    private String productDetail;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "location")
    private String location;
}
