package com.santanu.Spring_Security_Project.Model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long productId;
    private String productName;
    private String description;
    private String imageUrl;
    private int quantity;
    private BigDecimal price;
}
