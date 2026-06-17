package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.AlertDTO;
import com.coldchain.traceability.entity.Alert;
import com.coldchain.traceability.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertDetectionService alertDetectionService;

    public List<AlertDTO> getAllAlerts(String status) {
        List<Alert> alerts;
        if (status != null && !status.isEmpty()) {
            alerts = alertRepository.findByAlertStatusOrderByCreatedAtDesc(status);
        } else {
            alerts = alertRepository.findRecentAlerts(24);
        }
        return alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getPendingAlerts() {
        return alertDetectionService.getPendingAlerts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getAlertsForCargo(Long cargoBatchId) {
        return alertRepository.findByCargoBatchIdOrderByCreatedAtDesc(cargoBatchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AlertDTO getAlertById(Long id) {
        return alertRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
    }

    @Transactional
    public AlertDTO acknowledgeAlert(Long id, String acknowledgedBy) {
        Alert alert = alertDetectionService.acknowledgeAlert(id, acknowledgedBy);
        log.info("Alert {} acknowledged by {}", id, acknowledgedBy);
        return convertToDTO(alert);
    }

    @Transactional
    public AlertDTO resolveAlert(Long id, String acknowledgedBy) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(java.time.LocalDateTime.now());
        alert.setAlertStatus(Alert.AlertStatus.RESOLVED.name());

        alert = alertRepository.save(alert);
        log.info("Alert {} resolved by {}", id, acknowledgedBy);
        return convertToDTO(alert);
    }

    public Map<String, Long> getAlertStats() {
        long totalPending = alertRepository.findPendingAlerts().size();
        long criticalPending = alertRepository.countCriticalPendingAlerts();
        long total24h = alertRepository.findAlertsSince(java.time.LocalDateTime.now().minusHours(24)).size();

        return Map.of(
                "totalPending", totalPending,
                "criticalPending", criticalPending,
                "total24h", total24h
        );
    }

    private AlertDTO convertToDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAlertType(alert.getAlertType());
        dto.setAlertLevel(alert.getAlertLevel());
        dto.setAlertStatus(alert.getAlertStatus());
        dto.setCargoBatchId(alert.getCargoBatchId());
        dto.setCargoBatchNo(alert.getCargoBatchNo());
        dto.setVehiclePlate(alert.getVehiclePlate());
        dto.setFenceId(alert.getFenceId());
        dto.setFenceName(alert.getFenceName());
        dto.setLatitude(alert.getLatitude());
        dto.setLongitude(alert.getLongitude());
        dto.setTemperature(alert.getTemperature());
        dto.setMaxTemperature(alert.getMaxTemperature());
        dto.setConsecutiveCount(alert.getConsecutiveCount());
        dto.setMessage(alert.getMessage());
        dto.setAcknowledged(alert.getAcknowledged());
        dto.setAcknowledgedBy(alert.getAcknowledgedBy());
        dto.setAcknowledgedAt(alert.getAcknowledgedAt());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }
}
