package com.coldchain.traceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cargo_trace_logs", indexes = {
    @Index(name = "idx_cargo_batch_id", columnList = "cargo_batch_id"),
    @Index(name = "idx_trace_time", columnList = "traceTime"),
    @Index(name = "idx_cargo_trace_time", columnList = "cargo_batch_id, traceTime")
})
public class CargoTraceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_batch_id", nullable = false)
    private CargoBatch cargoBatch;

    @Column(nullable = false)
    private LocalDateTime traceTime;

    @Column(precision = 10, scale = 6)
    private Double latitude;

    @Column(precision = 10, scale = 6)
    private Double longitude;

    @Column(length = 200)
    private String locationName;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(length = 30)
    private String zoneCode;

    @Column(length = 50)
    private String vehiclePlate;

    @Column(length = 20)
    private String temperatureStatus;

    @Column(nullable = false)
    private Boolean gpsLost = false;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
