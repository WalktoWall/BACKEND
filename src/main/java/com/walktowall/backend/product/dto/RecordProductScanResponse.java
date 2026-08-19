package com.walktowall.backend.product.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordProductScanResponse {
    private String message;
    private Integer productId;
    private String productName;
    private String productImg;
}
