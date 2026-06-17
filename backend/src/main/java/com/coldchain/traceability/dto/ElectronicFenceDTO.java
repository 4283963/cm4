package com.coldchain.traceability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ElectronicFenceDTO {

    private Long id;

    @JsonProperty("fence_name")
    private String fenceName;

    @JsonProperty("fence_type")
    private String fenceType;

    @JsonProperty("coordinates")
    private List<List<Double>> coordinates;

    @JsonProperty("center_latitude")
    private Double centerLatitude;

    @JsonProperty("center_longitude")
    private Double centerLongitude;

    private Double radius;

    @JsonProperty("max_temperature")
    private BigDecimal maxTemperature;

    private String description;

    private Boolean enabled;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
