package com.santanu.Spring_Security_Project.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    //This is PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //One user can add multiple product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"email","username","password","emailVerified","profileImageUrl",
            "profileImagePublicId","role","address","city","zipCode","phoneNo"})
    private User user;

    @Column(unique = true, nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productDesc;

    //For Product Image
    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductImage> productImg;

    private BigDecimal productPrice;

    private String category;

    private String brand;

    private Integer quantity;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    public Integer getUserId() {
        return user != null ? user.getId() : null;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

}
