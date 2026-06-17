package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.CargoTraceDTO;
import com.coldchain.traceability.entity.CargoBatch;
import com.coldchain.traceability.service.CargoBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CargoBatchController {

    private final CargoBatchService cargoBatchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CargoTraceDTO.CargoInfoDTO>>> getAllInTransitCargos() {
        log.debug("Fetching all in-transit cargos");
        List<CargoTraceDTO.CargoInfoDTO> cargos = cargoBatchService.getAllInTransitCargos();
        return ResponseEntity.ok(ApiResponse.success(cargos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCargoBatch(
            @RequestBody Map<String, Object> request) {
        log.info("Creating new cargo batch: {}", request);

        String plateNumber = (String) request.get("plateNumber");
        String zoneCode = (String) request.get("zoneCode");

        CargoBatch cargo = new CargoBatch();
        cargo.setBatchNo((String) request.get("batchNo"));
        cargo.setCargoName((String) request.get("cargoName"));
        cargo.setCargoType((String) request.get("cargoType"));
        cargo.setOrigin((String) request.get("origin"));
        cargo.setDestination((String) request.get("destination"));
        cargo.setQuantity((Integer) request.get("quantity"));
        cargo.setUnit((String) request.getOrDefault("unit", "箱"));

        CargoBatch created = cargoBatchService.createCargoBatch(cargo, plateNumber, zoneCode);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("id", created.getId(),
                        "batchNo", created.getBatchNo(),
                        "status", "CREATED")));
    }
}
