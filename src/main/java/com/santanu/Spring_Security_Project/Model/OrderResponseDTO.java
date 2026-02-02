package com.santanu.Spring_Security_Project.Model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    private String username;
    private String email;

    private List<OrderItemResponseDTO> items;
}
