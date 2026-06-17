package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.GatewayDataDTO;
import com.coldchain.traceability.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MockDataController {

    private final MockDataService mockDataService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, String>>> initializeMockData() {
        log.info("Request to initialize mock data");
        mockDataService.initializeMockData();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "SUCCESS",
                        "message", "Mock data initialized")));
    }

    @GetMapping("/generate/{plateNumber}")
    public ResponseEntity<ApiResponse<GatewayDataDTO>> generateMockData(
            @PathVariable String plateNumber) {
        log.info("Generating mock data for vehicle: {}", plateNumber);
        try {
            GatewayDataDTO data = mockDataService.generateMockGatewayData(plateNumber);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping("/send/{plateNumber}")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockData(
            @PathVariable String plateNumber) {
        log.info("Sending mock data for vehicle: {}", plateNumber);
        try {
            mockDataService.sendMockData(plateNumber);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("status", "SUCCESS",
                            "message", "Mock data processed for " + plateNumber)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping("/send-all")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockDataForAll() {
        log.info("Sending mock data for all vehicles");
        mockDataService.sendMockDataForAllVehicles();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "SUCCESS",
                        "message", "Mock data processed for all vehicles")));
    }

    @PostMapping("/send-gps-lost/{plateNumber}")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockDataWithGpsLost(
            @PathVariable String plateNumber) {
        log.info("Sending mock data with GPS lost for vehicle: {}", plateNumber);
        try {
            mockDataService.sendMockDataWithGpsLost(plateNumber);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("status", "SUCCESS",
                            "message", "Mock data with GPS lost processed for " + plateNumber)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping("/send-all-gps-lost")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockDataWithGpsLostForAll() {
        log.info("Sending mock data with GPS lost for all vehicles");
        mockDataService.sendMockDataWithGpsLostForAllVehicles();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "SUCCESS",
                        "message", "Mock data with GPS lost processed for all vehicles")));
    }

    @PostMapping("/send-high-temp/{plateNumber}")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockDataWithHighTemperature(
            @PathVariable String plateNumber) {
        log.info("Sending mock data with HIGH TEMPERATURE for vehicle: {}", plateNumber);
        try {
            mockDataService.sendMockDataWithHighTemperature(plateNumber, true);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("status", "SUCCESS",
                            "message", "Mock data with high temperature processed for " + plateNumber)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping("/send-all-high-temp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendMockDataWithHighTempForAll() {
        log.info("Sending mock data with HIGH TEMPERATURE for all vehicles");
        mockDataService.sendMockDataWithHighTempForAllVehicles();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "SUCCESS",
                        "message", "Mock data with high temperature processed for all vehicles")));
    }
}
