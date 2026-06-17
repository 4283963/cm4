package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.VehicleStatusDTO;
import com.coldchain.traceability.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleStatusDTO>>> getAllActiveVehicles() {
        log.debug("Fetching all active vehicles");
        List<VehicleStatusDTO> vehicles = vehicleService.getAllActiveVehicles();
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/{plateNumber}")
    public ResponseEntity<ApiResponse<VehicleStatusDTO>> getVehicleStatus(
            @PathVariable String plateNumber) {
        log.debug("Fetching status for vehicle: {}", plateNumber);
        try {
            VehicleStatusDTO status = vehicleService.getVehicleStatus(plateNumber);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
