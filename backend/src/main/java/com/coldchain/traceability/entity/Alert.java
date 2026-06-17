package com.coldchain.traceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_status", columnList = "alert_status"),
    @Index(name = "idx_alert_cargo_batch", columnList = "cargo_batch_id"),
    @Index(name = "idx_alert_created", columnList = "created_at DESC")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 20)
    private String alertLevel;

    @Column(nullable = false, length = 20)
    private String alertStatus = "PENDING";

    private Long cargoBatchId;

    @Column(length = 50)
    private String cargoBatchNo;

    @Column(length = 20)
    private String vehiclePlate;

    private Long fenceId;

    @Column(length = 100)
    private String fenceName;

    private Double latitude;

    private Double longitude;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal maxTemperature;

    private Integer consecutiveCount;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column(length = 50)
    private String acknowledgedBy;

    private LocalDateTime acknowledgedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AlertType {
        TEMPERATURE_BROKEN_CHAIN,
        GEO_FENCE_BREACH
    }

    public enum AlertLevel {
        WARNING,
        CRITICAL
    }

    public enum AlertStatus {
        PENDING,
        ACKNOWLEDGED,
        RESOLVED
    }
}
