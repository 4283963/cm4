package com.coldchain.traceability.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VehicleStatusDTO {

    private Long id;
    private String plateNumber;
    private String driverName;
    private String driverPhone;
    private String model;
    private String status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastUpdateTime;
    private List<TemperatureZoneStatusDTO> temperatureZones;
    private List<CargoSummaryDTO> cargos;

    @Data
    public static class TemperatureZoneStatusDTO {
        private Long id;
        private String zoneCode;
        private String zoneName;
        private String zoneType;
        private BigDecimal minTemperature;
        private BigDecimal maxTemperature;
        private BigDecimal currentTemperature;
        private String temperatureStatus;
        private Integer cargoCount;
    }

    @Data
    public static class CargoSummaryDTO {
        private Long id;
        private String batchNo;
        private String cargoName;
        private String cargoType;
        private Integer quantity;
        private String unit;
        private String status;
        private String zoneCode;
        private LocalDateTime loadingTime;
        private LocalDateTime expectedArrivalTime;
    }
}
