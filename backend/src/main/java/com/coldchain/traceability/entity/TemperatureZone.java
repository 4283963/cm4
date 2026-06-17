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
@Table(name = "temperature_zones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"vehicle_id", "zone_code"})
})
public class TemperatureZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, length = 30)
    private String zoneCode;

    @Column(nullable = false, length = 100)
    private String zoneName;

    @Column(precision = 5, scale = 2)
    private BigDecimal minTemperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal maxTemperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal currentTemperature;

    private Integer capacity;

    @Column(length = 20)
    private String zoneType;

    @OneToMany(mappedBy = "temperatureZone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargoBatch> cargoBatches = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
