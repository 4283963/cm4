package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.GatewayDataDTO;
import com.coldchain.traceability.entity.*;
import com.coldchain.traceability.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayDataService {

    private final VehicleRepository vehicleRepository;
    private final TemperatureZoneRepository temperatureZoneRepository;
    private final CargoBatchRepository cargoBatchRepository;
    private final CargoTraceLogRepository traceLogRepository;

    @Async
    @Transactional
    public void processGatewayData(GatewayDataDTO gatewayData) {
        log.info("Processing gateway data: messageId={}, vehicle={}",
                gatewayData.getMessageId(),
                gatewayData.getVehicle().getPlateNumber());

        String plateNumber = gatewayData.getVehicle().getPlateNumber();
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseGet(() -> createNewVehicle(gatewayData));

        updateVehiclePosition(vehicle, gatewayData);
        List<CargoTraceLog> traceLogs = new ArrayList<>();

        for (GatewayDataDTO.TemperatureZoneDataDTO zoneData : gatewayData.getTemperatureZones()) {
            TemperatureZone zone = updateTemperatureZone(vehicle, zoneData);
            List<CargoTraceLog> zoneTraceLogs = createTraceLogsForZone(
                    vehicle, zone, zoneData, gatewayData);
            traceLogs.addAll(zoneTraceLogs);
        }

        if (!traceLogs.isEmpty()) {
            traceLogRepository.saveAll(traceLogs);
            log.info("Saved {} trace logs for vehicle {}", traceLogs.size(), plateNumber);
        }

        vehicle.setLastUpdateTime(LocalDateTime.now());
        vehicleRepository.save(vehicle);
    }

    private Vehicle createNewVehicle(GatewayDataDTO gatewayData) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(gatewayData.getVehicle().getPlateNumber());
        vehicle.setDriverName("未知司机");
        vehicle.setStatus("TRANSIT");
        vehicle.setModel("冷藏车");
        vehicle.setTotalCapacity(0);
        return vehicleRepository.save(vehicle);
    }

    private void updateVehiclePosition(Vehicle vehicle, GatewayDataDTO gatewayData) {
        GatewayDataDTO.GpsDataDTO gps = gatewayData.getGps();
        if (gps != null) {
            vehicle.setCurrentLatitude(gps.getLatitude());
            vehicle.setCurrentLongitude(gps.getLongitude());
        }
    }

    private TemperatureZone updateTemperatureZone(Vehicle vehicle,
                                                   GatewayDataDTO.TemperatureZoneDataDTO zoneData) {
        TemperatureZone zone = temperatureZoneRepository
                .findByVehiclePlateAndZoneCode(vehicle.getPlateNumber(), zoneData.getZoneCode())
                .orElseGet(() -> createNewZone(vehicle, zoneData));

        Double avgTemp = zoneData.getAvgTemperature();
        if (avgTemp != null) {
            zone.setCurrentTemperature(BigDecimal.valueOf(avgTemp));
        }

        List<GatewayDataDTO.SensorDataDTO> sensors = zoneData.getSensors();
        if (sensors != null && !sensors.isEmpty()) {
            for (GatewayDataDTO.SensorDataDTO sensor : sensors) {
                if ("temperature".equalsIgnoreCase(sensor.getSensorType())) {
                    zone.setCurrentTemperature(BigDecimal.valueOf(sensor.getValue()));
                    break;
                }
            }
        }

        return temperatureZoneRepository.save(zone);
    }

    private TemperatureZone createNewZone(Vehicle vehicle, GatewayDataDTO.TemperatureZoneDataDTO zoneData) {
        TemperatureZone zone = new TemperatureZone();
        zone.setVehicle(vehicle);
        zone.setZoneCode(zoneData.getZoneCode());
        zone.setZoneName(zoneData.getZoneName());
        zone.setMinTemperature(BigDecimal.valueOf(-18));
        zone.setMaxTemperature(BigDecimal.valueOf(-12));
        zone.setCurrentTemperature(BigDecimal.ZERO);
        zone.setCapacity(0);
        zone.setZoneType(determineZoneType(zoneData.getZoneCode()));
        return zone;
    }

    private String determineZoneType(String zoneCode) {
        String code = zoneCode.toLowerCase();
        if (code.contains("frozen") || code.contains("冷冻") || code.contains("ld")) {
            return "FROZEN";
        } else if (code.contains("chill") || code.contains("冷藏") || code.contains("lc")) {
            return "CHILLED";
        } else if (code.contains("fresh") || code.contains("保鲜") || code.contains("bx")) {
            return "FRESH";
        }
        return "CHILLED";
    }

    private List<CargoTraceLog> createTraceLogsForZone(
            Vehicle vehicle,
            TemperatureZone zone,
            GatewayDataDTO.TemperatureZoneDataDTO zoneData,
            GatewayDataDTO gatewayData) {

        List<CargoTraceLog> traceLogs = new ArrayList<>();
        List<CargoBatch> cargoBatches = cargoBatchRepository
                .findInTransitByVehiclePlate(vehicle.getPlateNumber());

        BigDecimal zoneTemperature = zone.getCurrentTemperature();
        BigDecimal zoneHumidity = extractHumidity(zoneData);

        for (CargoBatch cargo : cargoBatches) {
            if (cargo.getTemperatureZone() != null &&
                    cargo.getTemperatureZone().getZoneCode().equals(zone.getZoneCode())) {

                CargoTraceLog traceLog = new CargoTraceLog();
                traceLog.setCargoBatch(cargo);
                traceLog.setTraceTime(gatewayData.getTimestamp() != null ?
                        gatewayData.getTimestamp() : LocalDateTime.now());

                if (gatewayData.getGps() != null) {
                    traceLog.setLatitude(gatewayData.getGps().getLatitude());
                    traceLog.setLongitude(gatewayData.getGps().getLongitude());
                    traceLog.setLocationName(gatewayData.getGps().getLocationName());
                }

                traceLog.setTemperature(zoneTemperature);
                traceLog.setHumidity(zoneHumidity);
                traceLog.setZoneCode(zone.getZoneCode());
                traceLog.setVehiclePlate(vehicle.getPlateNumber());
                traceLog.setTemperatureStatus(determineTemperatureStatus(cargo, zoneTemperature));

                traceLogs.add(traceLog);
            }
        }

        return traceLogs;
    }

    private BigDecimal extractHumidity(GatewayDataDTO.TemperatureZoneDataDTO zoneData) {
        if (zoneData.getSensors() != null) {
            for (GatewayDataDTO.SensorDataDTO sensor : zoneData.getSensors()) {
                if ("humidity".equalsIgnoreCase(sensor.getSensorType())) {
                    return BigDecimal.valueOf(sensor.getValue());
                }
            }
        }
        return null;
    }

    private String determineTemperatureStatus(CargoBatch cargo, BigDecimal currentTemp) {
        if (currentTemp == null) {
            return "UNKNOWN";
        }
        BigDecimal minTemp = cargo.getRequiredMinTemp();
        BigDecimal maxTemp = cargo.getRequiredMaxTemp();

        if (minTemp == null || maxTemp == null) {
            TemperatureZone zone = cargo.getTemperatureZone();
            if (zone != null) {
                minTemp = zone.getMinTemperature();
                maxTemp = zone.getMaxTemperature();
            }
        }

        if (minTemp != null && maxTemp != null) {
            if (currentTemp.compareTo(minTemp) < 0 || currentTemp.compareTo(maxTemp) > 0) {
                return "ABNORMAL";
            }
            return "NORMAL";
        }

        return "UNKNOWN";
    }
}
