package com.coldchain.traceability.controller;

import com.coldchain.traceability.dto.ApiResponse;
import com.coldchain.traceability.dto.ElectronicFenceDTO;
import com.coldchain.traceability.service.ElectronicFenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/fences")
@RequiredArgsConstructor
public class ElectronicFenceController {

    private final ElectronicFenceService fenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ElectronicFenceDTO>>> getAllFences(
            @RequestParam(required = false) Boolean activeOnly) {
        List<ElectronicFenceDTO> fences = Boolean.TRUE.equals(activeOnly)
                ? fenceService.getActiveFences()
                : fenceService.getAllFences();
        return ResponseEntity.ok(ApiResponse.success(fences));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectronicFenceDTO>> getFenceById(@PathVariable Long id) {
        try {
            ElectronicFenceDTO fence = fenceService.getFenceById(id);
            return ResponseEntity.ok(ApiResponse.success(fence));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ElectronicFenceDTO>> createFence(
            @RequestBody ElectronicFenceDTO dto) {
        log.info("Creating electronic fence: {}", dto.getFenceName());
        ElectronicFenceDTO created = fenceService.createFence(dto);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectronicFenceDTO>> updateFence(
            @PathVariable Long id,
            @RequestBody ElectronicFenceDTO dto) {
        try {
            log.info("Updating electronic fence: {}", id);
            ElectronicFenceDTO updated = fenceService.updateFence(id, dto);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteFence(@PathVariable Long id) {
        try {
            log.info("Deleting electronic fence: {}", id);
            fenceService.deleteFence(id);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("status", "SUCCESS", "message", "Fence deleted")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(404, e.getMessage()));
        }
    }
}
