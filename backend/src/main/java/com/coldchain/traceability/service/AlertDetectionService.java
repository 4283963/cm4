package com.coldchain.traceability.service;

import com.coldchain.traceability.entity.*;
import com.coldchain.traceability.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertDetectionService {

    private final ElectronicFenceRepository fenceRepository;
    private final AlertRepository alertRepository;
    private final CargoBatchRepository cargoBatchRepository;
    private final TemperatureZoneRepository temperatureZoneRepository;
    private final ObjectMapper objectMapper;

    private static final int CONSECUTIVE_OVER_TEMP_THRESHOLD = 3;

    @Transactional
    public void detectAlerts(Vehicle vehicle, Double latitude, Double longitude,
                             GatewayDataDTO gatewayData) {
        if (latitude == null || longitude == null) {
            log.debug("Skipping alert detection: no valid GPS coordinates for vehicle {}",
                    vehicle.getPlateNumber());
            return;
        }

        try {
            List<ElectronicFence> activeFences = fenceRepository.findActivePolygonFences();

            for (ElectronicFence fence : activeFences) {
                boolean insideFence = isPointInPolygon(latitude, longitude, fence.getCoordinates());

                if (insideFence) {
                    log.debug("Vehicle {} is inside fence '{}' at {}, {}",
                            vehicle.getPlateNumber(), fence.getFenceName(), latitude, longitude);
                    checkTemperatureForFence(vehicle, fence, gatewayData, latitude, longitude);
                }
            }
        } catch (Exception e) {
            log.error("Error during alert detection for vehicle {}: {}",
                    vehicle.getPlateNumber(), e.getMessage(), e);
        }
    }

    private boolean isPointInPolygon(Double lat, Double lng, String coordinatesJson) {
        try {
            List<List<Double>> polygon = objectMapper.readValue(
                    coordinatesJson,
                    new TypeReference<List<List<Double>>>() {}
            );
            return isPointInPolygon(lat, lng, polygon);
        } catch (Exception e) {
            log.error("Error parsing polygon coordinates: {}", e.getMessage());
            return false;
        }
    }

    public boolean isPointInPolygon(Double lat, Double lng, List<List<Double>> polygon) {
        if (polygon == null || polygon.size() < 3) {
            return false;
        }

        boolean inside = false;
        int n = polygon.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon.get(i).get(0);
            double yi = polygon.get(i).get(1);
            double xj = polygon.get(j).get(0);
            double yj = polygon.get(j).get(1);

            if (((yi > lng) != (yj > lng)) &&
                (lat < (xj - xi) * (lng - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }

    private void checkTemperatureForFence(Vehicle vehicle, ElectronicFence fence,
                                          GatewayDataDTO gatewayData,
                                          Double latitude, Double longitude) {
        if (gatewayData.getTemperatureZones() == null) {
            return;
        }

        for (GatewayDataDTO.TemperatureZoneDataDTO zoneData : gatewayData.getTemperatureZones()) {
            TemperatureZone zone = temperatureZoneRepository
                    .findByVehiclePlateAndZoneCode(vehicle.getPlateNumber(), zoneData.getZoneCode())
                    .orElse(null);

            if (zone == null) {
                continue;
            }

            BigDecimal currentTemp = zoneData.getAvgTemperature();
            BigDecimal maxAllowedTemp = fence.getMaxTemperature();

            zone.setLastTemperatureCheck(LocalDateTime.now());

            if (currentTemp != null && currentTemp.compareTo(maxAllowedTemp) > 0) {
                zone.setConsecutiveOverTempCount(zone.getConsecutiveOverTempCount() + 1);
                log.warn("Zone {} temperature {:.2f}°C exceeds fence limit {:.2f}°C, consecutive count: {}",
                        zone.getZoneCode(), currentTemp, maxAllowedTemp, zone.getConsecutiveOverTempCount());

                if (zone.getConsecutiveOverTempCount() >= CONSECUTIVE_OVER_TEMP_THRESHOLD) {
                    triggerBrokenChainAlert(vehicle, zone, fence, currentTemp,
                            maxAllowedTemp, latitude, longitude);
                }
            } else {
                if (zone.getConsecutiveOverTempCount() > 0) {
                    log.info("Zone {} temperature restored to normal, resetting consecutive count",
                            zone.getZoneCode());
                }
                zone.setConsecutiveOverTempCount(0);
            }

            temperatureZoneRepository.save(zone);
        }
    }

    @Transactional
    protected void triggerBrokenChainAlert(Vehicle vehicle, TemperatureZone zone,
                                            ElectronicFence fence, BigDecimal currentTemp,
                                            BigDecimal maxAllowedTemp,
                                            Double latitude, Double longitude) {
        List<CargoBatch> cargoBatches = cargoBatchRepository
                .findInTransitByVehiclePlateAndZoneCode(
                        vehicle.getPlateNumber(), zone.getZoneCode());

        for (CargoBatch cargo : cargoBatches) {
            if ("BROKEN_CHAIN".equals(cargo.getAlertStatus())) {
                log.debug("Cargo {} already has BROKEN_CHAIN status, skipping", cargo.getBatchNo());
                continue;
            }

            Alert existingAlert = alertRepository
                    .findLatestPendingAlertForCargo(cargo.getId())
                    .orElse(null);

            if (existingAlert != null && "TEMPERATURE_BROKEN_CHAIN".equals(existingAlert.getAlertType())) {
                log.debug("Pending alert already exists for cargo {}", cargo.getBatchNo());
                continue;
            }

            Alert alert = new Alert();
            alert.setAlertType(Alert.AlertType.TEMPERATURE_BROKEN_CHAIN.name());
            alert.setAlertLevel(Alert.AlertLevel.CRITICAL.name());
            alert.setAlertStatus(Alert.AlertStatus.PENDING.name());
            alert.setCargoBatchId(cargo.getId());
            alert.setCargoBatchNo(cargo.getBatchNo());
            alert.setVehiclePlate(vehicle.getPlateNumber());
            alert.setFenceId(fence.getId());
            alert.setFenceName(fence.getFenceName());
            alert.setLatitude(latitude);
            alert.setLongitude(longitude);
            alert.setTemperature(currentTemp);
            alert.setMaxTemperature(maxAllowedTemp);
            alert.setConsecutiveCount(zone.getConsecutiveOverTempCount());
            alert.setMessage(String.format(
                    "货品【%s】进入电子围栏【%s】后，温度连续%d次超标：当前%.2f°C，限值%.2f°C，货品已变质拦截！",
                    cargo.getCargoName(), fence.getFenceName(),
                    zone.getConsecutiveOverTempCount(), currentTemp, maxAllowedTemp
            ));
            alert.setAcknowledged(false);

            alert = alertRepository.save(alert);

            cargo.setAlertStatus("BROKEN_CHAIN");
            cargo.setLastAlertId(alert.getId());
            cargoBatchRepository.save(cargo);

            log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            log.error("BROKEN CHAIN ALERT TRIGGERED!");
            log.error("Cargo: {} ({})", cargo.getCargoName(), cargo.getBatchNo());
            log.error("Vehicle: {}", vehicle.getPlateNumber());
            log.error("Fence: {}", fence.getFenceName());
            log.error("Temperature: {}°C (limit: {}°C)", currentTemp, maxAllowedTemp);
            log.error("Consecutive count: {}", zone.getConsecutiveOverTempCount());
            log.error("Alert ID: {}", alert.getId());
            log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public List<Alert> getPendingAlerts() {
        return alertRepository.findPendingAlerts();
    }

    public List<Alert> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertRepository.findAlertsSince(since);
    }

    @Transactional
    public Alert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAlertStatus(Alert.AlertStatus.ACKNOWLEDGED.name());

        return alertRepository.save(alert);
    }
}
