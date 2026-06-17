package com.coldchain.traceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cargo_batches")
public class CargoBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String batchNo;

    @Column(nullable = false, length = 200)
    private String cargoName;

    @Column(length = 100)
    private String cargoType;

    @Column(length = 200)
    private String origin;

    @Column(length = 200)
    private String destination;

    private Integer quantity;

    @Column(length = 20)
    private String unit;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 5, scale = 2)
    private BigDecimal requiredMinTemp;

    @Column(precision = 5, scale = 2)
    private BigDecimal requiredMaxTemp;

    @Column(length = 20)
    private String status;

    private LocalDateTime loadingTime;

    private LocalDateTime expectedArrivalTime;

    private LocalDateTime actualArrivalTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temperature_zone_id")
    private TemperatureZone temperatureZone;

    @OneToMany(mappedBy = "cargoBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargoTraceLog> traceLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
