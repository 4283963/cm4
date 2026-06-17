package com.coldchain.traceability.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CargoTraceDTO {

    private CargoInfoDTO cargoInfo;
    private List<TracePointDTO> tracePoints;
    private TemperatureStatsDTO temperatureStats;

    @Data
    public static class CargoInfoDTO {
        private Long id;
        private String batchNo;
        private String cargoName;
        private String cargoType;
        private String origin;
        private String destination;
        private Integer quantity;
        private String unit;
        private BigDecimal weight;
        private BigDecimal requiredMinTemp;
        private BigDecimal requiredMaxTemp;
        private String status;
        private LocalDateTime loadingTime;
        private LocalDateTime expectedArrivalTime;
        private String vehiclePlate;
        private String zoneCode;
        private String zoneName;
    }

    @Data
    public static class TracePointDTO {
        private LocalDateTime traceTime;
        private Double latitude;
        private Double longitude;
        private String locationName;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private String temperatureStatus;
        private String zoneCode;
        private String vehiclePlate;
        private String remark;
    }

    @Data
    public static class TemperatureStatsDTO {
        private BigDecimal minTemp;
        private BigDecimal maxTemp;
        private BigDecimal avgTemp;
        private long totalPoints;
        private long abnormalPoints;
        private double abnormalRate;
        private List<TemperatureAlertDTO> alerts;
    }

    @Data
    public static class TemperatureAlertDTO {
        private LocalDateTime alertTime;
        private BigDecimal temperature;
        private String locationName;
        private Double latitude;
        private Double longitude;
        private String alertType;
    }
}
