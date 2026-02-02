package com.santanu.Spring_Security_Project.Model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long cartId;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
}
