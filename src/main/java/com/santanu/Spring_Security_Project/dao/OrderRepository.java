package com.santanu.Spring_Security_Project.dao;

import com.santanu.Spring_Security_Project.Model.OrderModel;
import com.santanu.Spring_Security_Project.Model.Status;
import com.santanu.Spring_Security_Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, Long> {

    // ================= BASIC =================
    Optional<OrderModel> findByRazorpayOrderId(String razorpayOrderId);

    List<OrderModel> findByUser(User user);

    List<OrderModel> findByUser_Id(Integer userId);

    // ================= COUNT =================
    long countByStatus(Status status);

    // ================= TOTAL PAID AMOUNT =================
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM OrderModel o
        WHERE o.status = com.santanu.Spring_Security_Project.Model.Status.PAID
    """)
    BigDecimal getTotalPaidAmount();

    // ================= LAST 30 DAYS PAID SALES =================
    @Query("""
        SELECT DATE(o.createdAt), COALESCE(SUM(o.totalAmount), 0)
        FROM OrderModel o
        WHERE o.status = com.santanu.Spring_Security_Project.Model.Status.PAID
          AND o.createdAt >= :fromDate
        GROUP BY DATE(o.createdAt)
        ORDER BY DATE(o.createdAt)
    """)
    List<Object[]> getLast30DaysSales(@Param("fromDate") LocalDateTime fromDate);
}
