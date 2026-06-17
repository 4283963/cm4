package com.coldchain.traceability.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GatewayDataDTO {

    @JsonProperty("gateway_id")
    private String gatewayId;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("vehicle")
    private VehicleInfoDTO vehicle;

    @JsonProperty("gps")
    private GpsDataDTO gps;

    @JsonProperty("temperature_zones")
    private List<TemperatureZoneDataDTO> temperatureZones;

    @JsonProperty("device_status")
    private DeviceStatusDTO deviceStatus;

    @Data
    public static class VehicleInfoDTO {
        @JsonProperty("plate_number")
        private String plateNumber;

        @JsonProperty("vehicle_id")
        private String vehicleId;

        @JsonProperty("driver_id")
        private String driverId;
    }

    @Data
    public static class GpsDataDTO {
        @JsonProperty("coords")
        private List<CoordDTO> coords;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("altitude")
        private Double altitude;

        @JsonProperty("speed")
        private Double speed;

        @JsonProperty("heading")
        private Double heading;

        @JsonProperty("satellites")
        private Integer satellites;

        @JsonProperty("location_name")
        private String locationName;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;

        public boolean hasValidCoords() {
            return coords != null && !coords.isEmpty();
        }

        public Double getFirstLatitude() {
            return hasValidCoords() ? coords.get(0).getLatitude() : null;
        }

        public Double getFirstLongitude() {
            return hasValidCoords() ? coords.get(0).getLongitude() : null;
        }

        public Double getEffectiveLatitude() {
            if (latitude != null) return latitude;
            return getFirstLatitude();
        }

        public Double getEffectiveLongitude() {
            if (longitude != null) return longitude;
            return getFirstLongitude();
        }
    }

    @Data
    public static class CoordDTO {
        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("altitude")
        private Double altitude;

        @JsonProperty("accuracy")
        private Double accuracy;
    }

    @Data
    public static class TemperatureZoneDataDTO {
        @JsonProperty("zone_code")
        private String zoneCode;

        @JsonProperty("zone_name")
        private String zoneName;

        @JsonProperty("sensors")
        private List<SensorDataDTO> sensors;

        @JsonProperty("avg_temperature")
        private Double avgTemperature;

        @JsonProperty("status")
        private String status;
    }

    @Data
    public static class SensorDataDTO {
        @JsonProperty("sensor_id")
        private String sensorId;

        @JsonProperty("sensor_type")
        private String sensorType;

        @JsonProperty("value")
        private Double value;

        @JsonProperty("unit")
        private String unit;

        @JsonProperty("status")
        private String status;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    public static class DeviceStatusDTO {
        @JsonProperty("battery_level")
        private Integer batteryLevel;

        @JsonProperty("signal_strength")
        private Integer signalStrength;

        @JsonProperty("connection_status")
        private String connectionStatus;
    }
}
