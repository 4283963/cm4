package com.coldchain.traceability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertDTO {

    private Long id;

    @JsonProperty("alert_type")
    private String alertType;

    @JsonProperty("alert_level")
    private String alertLevel;

    @JsonProperty("alert_status")
    private String alertStatus;

    @JsonProperty("cargo_batch_id")
    private Long cargoBatchId;

    @JsonProperty("cargo_batch_no")
    private String cargoBatchNo;

    @JsonProperty("vehicle_plate")
    private String vehiclePlate;

    @JsonProperty("fence_id")
    private Long fenceId;

    @JsonProperty("fence_name")
    private String fenceName;

    private Double latitude;

    private Double longitude;

    private BigDecimal temperature;

    @JsonProperty("max_temperature")
    private BigDecimal maxTemperature;

    @JsonProperty("consecutive_count")
    private Integer consecutiveCount;

    private String message;

    private Boolean acknowledged;

    @JsonProperty("acknowledged_by")
    private String acknowledgedBy;

    @JsonProperty("acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
