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
    private final AlertDetectionService alertDetectionService;

    @Async
    @Transactional
    public void processGatewayData(GatewayDataDTO gatewayData) {
        String plateNumber = gatewayData.getVehicle().getPlateNumber();
        log.info("Processing gateway data: messageId={}, vehicle={}",
                gatewayData.getMessageId(), plateNumber);

        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseGet(() -> createNewVehicle(gatewayData));

        boolean gpsLost = false;
        Double effectiveLat = vehicle.getCurrentLatitude();
        Double effectiveLng = vehicle.getCurrentLongitude();
        String effectiveLocation = null;

        try {
            GatewayDataDTO.GpsDataDTO gps = gatewayData.getGps();
            if (gps != null) {
                Double lat = gps.getEffectiveLatitude();
                Double lng = gps.getEffectiveLongitude();
                if (lat != null && lng != null) {
                    effectiveLat = lat;
                    effectiveLng = lng;
                    effectiveLocation = gps.getLocationName();
                    vehicle.setCurrentLatitude(lat);
                    vehicle.setCurrentLongitude(lng);
                    log.debug("GPS valid for {}: {}, {}", plateNumber, lat, lng);
                } else {
                    gpsLost = true;
                    log.warn("GPS signal lost for vehicle {}, using last known position: {}, {}",
                            plateNumber, effectiveLat, effectiveLng);
                }
            } else {
                gpsLost = true;
                log.warn("GPS data null for vehicle {}, using last known position", plateNumber);
            }
        } catch (Exception e) {
            gpsLost = true;
            log.error("Error parsing GPS data for vehicle {}, using last known position. Error: {}",
                    plateNumber, e.getMessage(), e);
        }

        List<CargoTraceLog> traceLogs = new ArrayList<>();

        if (gatewayData.getTemperatureZones() != null) {
            for (GatewayDataDTO.TemperatureZoneDataDTO zoneData : gatewayData.getTemperatureZones()) {
                try {
                    TemperatureZone zone = updateTemperatureZone(vehicle, zoneData);
                    List<CargoTraceLog> zoneTraceLogs = createTraceLogsForZone(
                            vehicle, zone, zoneData, gatewayData,
                            effectiveLat, effectiveLng, effectiveLocation, gpsLost);
                    traceLogs.addAll(zoneTraceLogs);
                } catch (Exception e) {
                    log.error("Error processing zone {} for vehicle {}, skipping this zone but continuing others. Error: {}",
                            zoneData.getZoneCode(), plateNumber, e.getMessage(), e);
                }
            }
        }

        if (!traceLogs.isEmpty()) {
            traceLogRepository.saveAll(traceLogs);
            log.info("Saved {} trace logs for vehicle {} (gpsLost={})",
                    traceLogs.size(), plateNumber, gpsLost);
        } else {
            log.warn("No trace logs generated for vehicle {}, temperature data may be missing", plateNumber);
        }

        vehicle.setLastUpdateTime(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        alertDetectionService.detectAlerts(vehicle, effectiveLat, effectiveLng, gatewayData);
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
            GatewayDataDTO gatewayData,
            Double effectiveLat,
            Double effectiveLng,
            String effectiveLocation,
            boolean gpsLost) {

        List<CargoTraceLog> traceLogs = new ArrayList<>();
        List<CargoBatch> cargoBatches = cargoBatchRepository
                .findInTransitByVehiclePlate(vehicle.getPlateNumber());

        BigDecimal zoneTemperature = zone.getCurrentTemperature();
        BigDecimal zoneHumidity = extractHumidity(zoneData);

        if (cargoBatches == null) {
            return traceLogs;
        }

        for (CargoBatch cargo : cargoBatches) {
            try {
                if (cargo.getTemperatureZone() != null &&
                        cargo.getTemperatureZone().getZoneCode().equals(zone.getZoneCode())) {

                    CargoTraceLog traceLog = new CargoTraceLog();
                    traceLog.setCargoBatch(cargo);
                    traceLog.setTraceTime(gatewayData.getTimestamp() != null ?
                            gatewayData.getTimestamp() : LocalDateTime.now());

                    traceLog.setLatitude(effectiveLat);
                    traceLog.setLongitude(effectiveLng);
                    traceLog.setLocationName(effectiveLocation);
                    traceLog.setGpsLost(gpsLost);

                    if (gpsLost) {
                        traceLog.setRemark("GPS信号丢失，使用最后已知位置回填");
                    }

                    traceLog.setTemperature(zoneTemperature);
                    traceLog.setHumidity(zoneHumidity);
                    traceLog.setZoneCode(zone.getZoneCode());
                    traceLog.setVehiclePlate(vehicle.getPlateNumber());
                    traceLog.setTemperatureStatus(determineTemperatureStatus(cargo, zoneTemperature));

                    traceLogs.add(traceLog);
                }
            } catch (Exception e) {
                log.error("Error creating trace log for cargo {} in zone {}, skipping. Error: {}",
                        cargo.getBatchNo(), zone.getZoneCode(), e.getMessage(), e);
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
