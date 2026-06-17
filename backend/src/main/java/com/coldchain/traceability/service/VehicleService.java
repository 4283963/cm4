package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.VehicleStatusDTO;
import com.coldchain.traceability.entity.CargoBatch;
import com.coldchain.traceability.entity.TemperatureZone;
import com.coldchain.traceability.entity.Vehicle;
import com.coldchain.traceability.repository.CargoBatchRepository;
import com.coldchain.traceability.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CargoBatchRepository cargoBatchRepository;

    @Transactional(readOnly = true)
    public List<VehicleStatusDTO> getAllActiveVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAllActiveVehiclesWithZones();
        return vehicles.stream()
                .map(this::convertToStatusDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleStatusDTO getVehicleStatus(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumberWithZones(plateNumber)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + plateNumber));
        return convertToStatusDTO(vehicle);
    }

    private VehicleStatusDTO convertToStatusDTO(Vehicle vehicle) {
        VehicleStatusDTO dto = new VehicleStatusDTO();
        dto.setId(vehicle.getId());
        dto.setPlateNumber(vehicle.getPlateNumber());
        dto.setDriverName(vehicle.getDriverName());
        dto.setDriverPhone(vehicle.getDriverPhone());
        dto.setModel(vehicle.getModel());
        dto.setStatus(vehicle.getStatus());
        dto.setCurrentLatitude(vehicle.getCurrentLatitude());
        dto.setCurrentLongitude(vehicle.getCurrentLongitude());
        dto.setLastUpdateTime(vehicle.getLastUpdateTime());

        List<VehicleStatusDTO.TemperatureZoneStatusDTO> zoneDTOs = vehicle.getTemperatureZones()
                .stream()
                .map(this::convertZoneToDTO)
                .collect(Collectors.toList());
        dto.setTemperatureZones(zoneDTOs);

        List<CargoBatch> cargos = cargoBatchRepository.findInTransitByVehiclePlate(vehicle.getPlateNumber());
        List<VehicleStatusDTO.CargoSummaryDTO> cargoDTOs = cargos.stream()
                .map(this::convertCargoToDTO)
                .collect(Collectors.toList());
        dto.setCargos(cargoDTOs);

        return dto;
    }

    private VehicleStatusDTO.TemperatureZoneStatusDTO convertZoneToDTO(TemperatureZone zone) {
        VehicleStatusDTO.TemperatureZoneStatusDTO dto = new VehicleStatusDTO.TemperatureZoneStatusDTO();
        dto.setId(zone.getId());
        dto.setZoneCode(zone.getZoneCode());
        dto.setZoneName(zone.getZoneName());
        dto.setZoneType(zone.getZoneType());
        dto.setMinTemperature(zone.getMinTemperature());
        dto.setMaxTemperature(zone.getMaxTemperature());
        dto.setCurrentTemperature(zone.getCurrentTemperature());
        dto.setTemperatureStatus(determineStatus(zone));
        dto.setCargoCount((int) zone.getCargoBatches().stream()
                .filter(c -> "IN_TRANSIT".equals(c.getStatus()))
                .count());
        return dto;
    }

    private String determineStatus(TemperatureZone zone) {
        BigDecimal current = zone.getCurrentTemperature();
        BigDecimal min = zone.getMinTemperature();
        BigDecimal max = zone.getMaxTemperature();

        if (current == null || min == null || max == null) {
            return "UNKNOWN";
        }

        if (current.compareTo(min) < 0 || current.compareTo(max) > 0) {
            return "ABNORMAL";
        }
        return "NORMAL";
    }

    private VehicleStatusDTO.CargoSummaryDTO convertCargoToDTO(CargoBatch cargo) {
        VehicleStatusDTO.CargoSummaryDTO dto = new VehicleStatusDTO.CargoSummaryDTO();
        dto.setId(cargo.getId());
        dto.setBatchNo(cargo.getBatchNo());
        dto.setCargoName(cargo.getCargoName());
        dto.setCargoType(cargo.getCargoType());
        dto.setQuantity(cargo.getQuantity());
        dto.setUnit(cargo.getUnit());
        dto.setStatus(cargo.getStatus());
        dto.setZoneCode(cargo.getTemperatureZone() != null ? cargo.getTemperatureZone().getZoneCode() : null);
        dto.setLoadingTime(cargo.getLoadingTime());
        dto.setExpectedArrivalTime(cargo.getExpectedArrivalTime());
        return dto;
    }
}
