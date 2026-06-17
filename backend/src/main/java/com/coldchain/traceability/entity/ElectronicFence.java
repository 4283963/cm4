package com.coldchain.traceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "electronic_fences")
public class ElectronicFence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fenceName;

    @Column(nullable = false, length = 20)
    private String fenceType = "POLYGON";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coordinates;

    private Double centerLatitude;

    private Double centerLongitude;

    private Double radius;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxTemperature;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
