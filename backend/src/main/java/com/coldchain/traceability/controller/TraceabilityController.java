package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.CargoTraceDTO;
import com.coldchain.traceability.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/traceability")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    @GetMapping("/cargo/{batchNo}")
    public ResponseEntity<ApiResponse<CargoTraceDTO>> getCargoTraceability(
            @PathVariable String batchNo) {
        log.debug("Fetching traceability for cargo batch: {}", batchNo);
        try {
            CargoTraceDTO trace = traceabilityService.getCargoTraceability(batchNo);
            return ResponseEntity.ok(ApiResponse.success(trace));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/cargo/{batchNo}/range")
    public ResponseEntity<ApiResponse<CargoTraceDTO>> getCargoTraceabilityByTimeRange(
            @PathVariable String batchNo,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        log.debug("Fetching traceability for cargo: {}, time range: {} - {}",
                batchNo, startTime, endTime);
        try {
            CargoTraceDTO trace = traceabilityService.getCargoTraceabilityByTimeRange(
                    batchNo, startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success(trace));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
