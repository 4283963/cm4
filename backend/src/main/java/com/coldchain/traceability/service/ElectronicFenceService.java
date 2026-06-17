package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.ElectronicFenceDTO;
import com.coldchain.traceability.entity.ElectronicFence;
import com.coldchain.traceability.repository.ElectronicFenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectronicFenceService {

    private final ElectronicFenceRepository fenceRepository;
    private final ObjectMapper objectMapper;

    public List<ElectronicFenceDTO> getAllFences() {
        return fenceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ElectronicFenceDTO> getActiveFences() {
        return fenceRepository.findByEnabledTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ElectronicFenceDTO getFenceById(Long id) {
        return fenceRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Fence not found: " + id));
    }

    @Transactional
    public ElectronicFenceDTO createFence(ElectronicFenceDTO dto) {
        ElectronicFence fence = convertToEntity(dto);
        fence = fenceRepository.save(fence);
        log.info("Created electronic fence: {} (id={})", fence.getFenceName(), fence.getId());
        return convertToDTO(fence);
    }

    @Transactional
    public ElectronicFenceDTO updateFence(Long id, ElectronicFenceDTO dto) {
        ElectronicFence fence = fenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fence not found: " + id));

        fence.setFenceName(dto.getFenceName());
        fence.setFenceType(dto.getFenceType());
        fence.setMaxTemperature(dto.getMaxTemperature());
        fence.setDescription(dto.getDescription());
        fence.setEnabled(dto.getEnabled());

        if (dto.getCoordinates() != null) {
            fence.setCoordinates(coordinatesToJson(dto.getCoordinates()));
        }
        if (dto.getCenterLatitude() != null) {
            fence.setCenterLatitude(dto.getCenterLatitude());
        }
        if (dto.getCenterLongitude() != null) {
            fence.setCenterLongitude(dto.getCenterLongitude());
        }
        if (dto.getRadius() != null) {
            fence.setRadius(dto.getRadius());
        }

        fence = fenceRepository.save(fence);
        log.info("Updated electronic fence: {} (id={})", fence.getFenceName(), fence.getId());
        return convertToDTO(fence);
    }

    @Transactional
    public void deleteFence(Long id) {
        if (!fenceRepository.existsById(id)) {
            throw new IllegalArgumentException("Fence not found: " + id);
        }
        fenceRepository.deleteById(id);
        log.info("Deleted electronic fence: id={}", id);
    }

    private ElectronicFenceDTO convertToDTO(ElectronicFence fence) {
        ElectronicFenceDTO dto = new ElectronicFenceDTO();
        dto.setId(fence.getId());
        dto.setFenceName(fence.getFenceName());
        dto.setFenceType(fence.getFenceType());
        dto.setCoordinates(parseCoordinates(fence.getCoordinates()));
        dto.setCenterLatitude(fence.getCenterLatitude());
        dto.setCenterLongitude(fence.getCenterLongitude());
        dto.setRadius(fence.getRadius());
        dto.setMaxTemperature(fence.getMaxTemperature());
        dto.setDescription(fence.getDescription());
        dto.setEnabled(fence.getEnabled());
        dto.setCreatedAt(fence.getCreatedAt());
        dto.setUpdatedAt(fence.getUpdatedAt());
        return dto;
    }

    private ElectronicFence convertToEntity(ElectronicFenceDTO dto) {
        ElectronicFence fence = new ElectronicFence();
        fence.setFenceName(dto.getFenceName());
        fence.setFenceType(dto.getFenceType() != null ? dto.getFenceType() : "POLYGON");
        fence.setCoordinates(coordinatesToJson(dto.getCoordinates()));
        fence.setCenterLatitude(dto.getCenterLatitude());
        fence.setCenterLongitude(dto.getCenterLongitude());
        fence.setRadius(dto.getRadius());
        fence.setMaxTemperature(dto.getMaxTemperature());
        fence.setDescription(dto.getDescription());
        fence.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        return fence;
    }

    private List<List<Double>> parseCoordinates(String coordinatesJson) {
        try {
            return objectMapper.readValue(
                    coordinatesJson,
                    new TypeReference<List<List<Double>>>() {}
            );
        } catch (JsonProcessingException e) {
            log.error("Error parsing coordinates JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private String coordinatesToJson(List<List<Double>> coordinates) {
        try {
            return objectMapper.writeValueAsString(coordinates);
        } catch (JsonProcessingException e) {
            log.error("Error serializing coordinates to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}
