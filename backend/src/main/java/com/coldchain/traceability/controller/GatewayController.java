package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.GatewayDataDTO;
import com.coldchain.traceability.service.GatewayDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GatewayController {

    private final GatewayDataService gatewayDataService;

    @PostMapping("/data")
    public ResponseEntity<ApiResponse<Map<String, String>>> receiveGatewayData(
            @Valid @RequestBody GatewayDataDTO gatewayData) {

        log.info("Received gateway data from: {}, messageId: {}",
                gatewayData.getGatewayId(),
                gatewayData.getMessageId());

        if (gatewayData.getVehicle() == null || gatewayData.getVehicle().getPlateNumber() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Vehicle plate number is required"));
        }

        gatewayDataService.processGatewayData(gatewayData);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("messageId", gatewayData.getMessageId(),
                        "status", "ACCEPTED")));
    }

    @PostMapping("/raw")
    public ResponseEntity<ApiResponse<Map<String, String>>> receiveRawData(
            @RequestBody Map<String, Object> rawData) {

        log.info("Received raw gateway data: {}", rawData);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "RECEIVED",
                        "note", "Use /api/gateway/data for structured data")));
    }
}
