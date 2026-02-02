package com.santanu.Spring_Security_Project.Service;

import com.santanu.Spring_Security_Project.Model.*;
import com.santanu.Spring_Security_Project.dao.OrderRepository;
import com.santanu.Spring_Security_Project.dao.ProductRepo;
import com.santanu.Spring_Security_Project.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class OrderService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepo productRepo;

    public List<OrderResponseDTO> getMyOrders(Authentication authentication) {

        User user = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::mapToOrderDTO)
                .toList();
    }

    public List<OrderResponseDTO> getUserOrder(
            Integer userId,
            Authentication authentication
    ) {

        User admin = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can see user orders");
        }

        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToOrderDTO)
                .toList();
    }

    public List<OrderResponseDTO> getAllUserOrder(Authentication authentication) {

        User admin = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can see user orders");
        }
        return orderRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(OrderModel::getCreatedAt).reversed())
                .map(this::mapToOrderDTO)
                .toList();
    }

    private OrderResponseDTO mapToOrderDTO(OrderModel order) {

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUsername(order.getUser().getUsername());
        dto.setEmail(order.getUser().getEmail());

        List<OrderItemResponseDTO> items =
                order.getItems().stream().map(item -> {

                    Product product = item.getProduct();

                    OrderItemResponseDTO itemDTO =
                            new OrderItemResponseDTO();

                    itemDTO.setProductId(product.getId());
                    itemDTO.setProductName(product.getProductName());
                    itemDTO.setDescription(product.getProductDesc());
                    itemDTO.setPrice(item.getProductPrice());
                    itemDTO.setQuantity(item.getQuantity());

                    // Safe image handling
                    if (product.getProductImg() != null &&
                            !product.getProductImg().isEmpty()) {
                        itemDTO.setImageUrl(
                                product.getProductImg().get(0).getUrl()
                        );
                    }

                    return itemDTO;

                }).toList();

        dto.setItems(items);
        return dto;
    }

    public Map<String, Object> getAdminPanel(Authentication authentication) {

        User admin = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN See Admin Panel");
        }

        Map<String,Object> result=new HashMap<>();
        result.put("totalUsers",userRepo.count());
        result.put("totalProducts",productRepo.count());
        result.put("totalOrders",orderRepository.countByStatus(Status.PAID));
        result.put("totalPaidAmount",orderRepository.getTotalPaidAmount());

        //for 30Days sales data
        LocalDateTime fromDate=LocalDateTime.now().minusDays(30);

        List<Map<String,Object>> sales=new ArrayList<>();
        for(Object[] row: orderRepository.getLast30DaysSales(fromDate))
        {
            Map<String,Object> day=new HashMap<>();
            day.put("day",row[0]);
            day.put("amount",row[1]);
            sales.add(day);
        }
        result.put("last30DaysSales", sales);
        return result;
    }
}
