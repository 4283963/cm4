package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.AlertDTO;
import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAllAlerts(
            @RequestParam(required = false) String status) {
        List<AlertDTO> alerts = alertService.getAllAlerts(status);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getPendingAlerts() {
        List<AlertDTO> alerts = alertService.getPendingAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAlertStats() {
        Map<String, Long> stats = alertService.getAlertStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertDTO>> getAlertById(@PathVariable Long id) {
        try {
            AlertDTO alert = alertService.getAlertById(id);
            return ResponseEntity.ok(ApiResponse.success(alert));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/cargo/{cargoBatchId}")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAlertsForCargo(
            @PathVariable Long cargoBatchId) {
        List<AlertDTO> alerts = alertService.getAlertsForCargo(cargoBatchId);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertDTO>> acknowledgeAlert(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String acknowledgedBy = request != null ? request.get("acknowledgedBy") : "system";
            log.info("Acknowledging alert {} by {}", id, acknowledgedBy);
            AlertDTO alert = alertService.acknowledgeAlert(id, acknowledgedBy);
            return ResponseEntity.ok(ApiResponse.success(alert));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertDTO>> resolveAlert(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String acknowledgedBy = request != null ? request.get("acknowledgedBy") : "system";
            log.info("Resolving alert {} by {}", id, acknowledgedBy);
            AlertDTO alert = alertService.resolveAlert(id, acknowledgedBy);
            return ResponseEntity.ok(ApiResponse.success(alert));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
