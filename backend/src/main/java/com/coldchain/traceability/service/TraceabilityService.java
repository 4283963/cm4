package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.CargoTraceDTO;
import com.coldchain.traceability.entity.CargoBatch;
import com.coldchain.traceability.entity.CargoTraceLog;
import com.coldchain.traceability.entity.TemperatureZone;
import com.coldchain.traceability.repository.CargoBatchRepository;
import com.coldchain.traceability.repository.CargoTraceLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraceabilityService {

    private final CargoBatchRepository cargoBatchRepository;
    private final CargoTraceLogRepository traceLogRepository;

    @Transactional(readOnly = true)
    public CargoTraceDTO getCargoTraceability(String batchNo) {
        CargoBatch cargo = cargoBatchRepository.findByBatchNoWithDetails(batchNo)
                .orElseThrow(() -> new IllegalArgumentException("Cargo batch not found: " + batchNo));

        List<CargoTraceLog> traceLogs = traceLogRepository.findByBatchNoOrderByTraceTime(batchNo);

        CargoTraceDTO dto = new CargoTraceDTO();
        dto.setCargoInfo(convertToCargoInfo(cargo));
        dto.setTracePoints(convertToTracePoints(traceLogs));
        dto.setTemperatureStats(calculateTemperatureStats(traceLogs, cargo));

        return dto;
    }

    @Transactional(readOnly = true)
    public CargoTraceDTO getCargoTraceabilityByTimeRange(String batchNo,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime) {
        CargoBatch cargo = cargoBatchRepository.findByBatchNoWithDetails(batchNo)
                .orElseThrow(() -> new IllegalArgumentException("Cargo batch not found: " + batchNo));

        List<CargoTraceLog> traceLogs = traceLogRepository.findByBatchNoAndTimeRange(batchNo, startTime, endTime);

        CargoTraceDTO dto = new CargoTraceDTO();
        dto.setCargoInfo(convertToCargoInfo(cargo));
        dto.setTracePoints(convertToTracePoints(traceLogs));
        dto.setTemperatureStats(calculateTemperatureStats(traceLogs, cargo));

        return dto;
    }

    private CargoTraceDTO.CargoInfoDTO convertToCargoInfo(CargoBatch cargo) {
        CargoTraceDTO.CargoInfoDTO info = new CargoTraceDTO.CargoInfoDTO();
        info.setId(cargo.getId());
        info.setBatchNo(cargo.getBatchNo());
        info.setCargoName(cargo.getCargoName());
        info.setCargoType(cargo.getCargoType());
        info.setOrigin(cargo.getOrigin());
        info.setDestination(cargo.getDestination());
        info.setQuantity(cargo.getQuantity());
        info.setUnit(cargo.getUnit());
        info.setWeight(cargo.getWeight());
        info.setRequiredMinTemp(cargo.getRequiredMinTemp());
        info.setRequiredMaxTemp(cargo.getRequiredMaxTemp());
        info.setStatus(cargo.getStatus());
        info.setLoadingTime(cargo.getLoadingTime());
        info.setExpectedArrivalTime(cargo.getExpectedArrivalTime());

        TemperatureZone zone = cargo.getTemperatureZone();
        if (zone != null) {
            info.setZoneCode(zone.getZoneCode());
            info.setZoneName(zone.getZoneName());
            if (zone.getVehicle() != null) {
                info.setVehiclePlate(zone.getVehicle().getPlateNumber());
            }
        }

        return info;
    }

    private List<CargoTraceDTO.TracePointDTO> convertToTracePoints(List<CargoTraceLog> logs) {
        return logs.stream()
                .map(this::convertToTracePoint)
                .collect(Collectors.toList());
    }

    private CargoTraceDTO.TracePointDTO convertToTracePoint(CargoTraceLog log) {
        CargoTraceDTO.TracePointDTO point = new CargoTraceDTO.TracePointDTO();
        point.setTraceTime(log.getTraceTime());
        point.setLatitude(log.getLatitude());
        point.setLongitude(log.getLongitude());
        point.setLocationName(log.getLocationName());
        point.setTemperature(log.getTemperature());
        point.setHumidity(log.getHumidity());
        point.setTemperatureStatus(log.getTemperatureStatus());
        point.setZoneCode(log.getZoneCode());
        point.setVehiclePlate(log.getVehiclePlate());
        point.setRemark(log.getRemark());
        return point;
    }

    private CargoTraceDTO.TemperatureStatsDTO calculateTemperatureStats(
            List<CargoTraceLog> logs, CargoBatch cargo) {

        CargoTraceDTO.TemperatureStatsDTO stats = new CargoTraceDTO.TemperatureStatsDTO();

        if (logs.isEmpty()) {
            stats.setTotalPoints(0);
            stats.setAbnormalPoints(0);
            stats.setAbnormalRate(0.0);
            return stats;
        }

        List<BigDecimal> temperatures = logs.stream()
                .map(CargoTraceLog::getTemperature)
                .filter(t -> t != null)
                .collect(Collectors.toList());

        if (!temperatures.isEmpty()) {
            stats.setMinTemp(temperatures.stream()
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO));
            stats.setMaxTemp(temperatures.stream()
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO));

            BigDecimal sum = temperatures.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setAvgTemp(sum.divide(BigDecimal.valueOf(temperatures.size()), 2, RoundingMode.HALF_UP));
        }

        stats.setTotalPoints(logs.size());

        long abnormalCount = logs.stream()
                .filter(log -> "ABNORMAL".equals(log.getTemperatureStatus()))
                .count();
        stats.setAbnormalPoints(abnormalCount);
        stats.setAbnormalRate((double) abnormalCount / logs.size() * 100);

        List<CargoTraceDTO.TemperatureAlertDTO> alerts = new ArrayList<>();
        BigDecimal minTemp = cargo.getRequiredMinTemp();
        BigDecimal maxTemp = cargo.getRequiredMaxTemp();

        if (minTemp == null && cargo.getTemperatureZone() != null) {
            minTemp = cargo.getTemperatureZone().getMinTemperature();
            maxTemp = cargo.getTemperatureZone().getMaxTemperature();
        }

        for (CargoTraceLog log : logs) {
            if ("ABNORMAL".equals(log.getTemperatureStatus()) && log.getTemperature() != null) {
                CargoTraceDTO.TemperatureAlertDTO alert = new CargoTraceDTO.TemperatureAlertDTO();
                alert.setAlertTime(log.getTraceTime());
                alert.setTemperature(log.getTemperature());
                alert.setLocationName(log.getLocationName());
                alert.setLatitude(log.getLatitude());
                alert.setLongitude(log.getLongitude());

                if (maxTemp != null && log.getTemperature().compareTo(maxTemp) > 0) {
                    alert.setAlertType("OVERHEAT");
                } else if (minTemp != null && log.getTemperature().compareTo(minTemp) < 0) {
                    alert.setAlertType("UNDERCOOL");
                } else {
                    alert.setAlertType("ABNORMAL");
                }

                alerts.add(alert);
            }
        }

        stats.setAlerts(alerts);
        return stats;
    }
}
