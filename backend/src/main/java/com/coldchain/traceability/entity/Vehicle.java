package com.coldchain.traceability.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String plateNumber;

    @Column(nullable = false, length = 100)
    private String driverName;

    @Column(length = 20)
    private String driverPhone;

    @Column(length = 100)
    private String model;

    private Integer totalCapacity;

    @Column(length = 20)
    private String status;

    @Column(precision = 10, scale = 6)
    private Double currentLatitude;

    @Column(precision = 10, scale = 6)
    private Double currentLongitude;

    private LocalDateTime lastUpdateTime;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemperatureZone> temperatureZones = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
