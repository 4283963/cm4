package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.CargoTraceDTO;
import com.coldchain.traceability.entity.CargoBatch;
import com.coldchain.traceability.entity.TemperatureZone;
import com.coldchain.traceability.entity.Vehicle;
import com.coldchain.traceability.repository.CargoBatchRepository;
import com.coldchain.traceability.repository.TemperatureZoneRepository;
import com.coldchain.traceability.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CargoBatchService {

    private final CargoBatchRepository cargoBatchRepository;
    private final VehicleRepository vehicleRepository;
    private final TemperatureZoneRepository temperatureZoneRepository;

    @Transactional
    public CargoBatch createCargoBatch(CargoBatch cargoBatch, String plateNumber, String zoneCode) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + plateNumber));

        TemperatureZone zone = temperatureZoneRepository
                .findByVehiclePlateAndZoneCode(plateNumber, zoneCode)
                .orElseGet(() -> {
                    TemperatureZone newZone = new TemperatureZone();
                    newZone.setVehicle(vehicle);
                    newZone.setZoneCode(zoneCode);
                    newZone.setZoneName(zoneCode);
                    newZone.setMinTemperature(BigDecimal.valueOf(-18));
                    newZone.setMaxTemperature(BigDecimal.valueOf(-12));
                    newZone.setCurrentTemperature(BigDecimal.valueOf(-15));
                    newZone.setZoneType("CHILLED");
                    return temperatureZoneRepository.save(newZone);
                });

        cargoBatch.setTemperatureZone(zone);
        if (cargoBatch.getStatus() == null) {
            cargoBatch.setStatus("IN_TRANSIT");
        }
        if (cargoBatch.getLoadingTime() == null) {
            cargoBatch.setLoadingTime(LocalDateTime.now());
        }
        return cargoBatchRepository.save(cargoBatch);
    }

    @Transactional(readOnly = true)
    public List<CargoTraceDTO.CargoInfoDTO> getAllInTransitCargos() {
        return cargoBatchRepository.findAllInTransitWithDetails().stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    private CargoTraceDTO.CargoInfoDTO convertToInfoDTO(CargoBatch cargo) {
        CargoTraceDTO.CargoInfoDTO info = new CargoTraceDTO.CargoInfoDTO();
        info.setId(cargo.getId());
        info.setBatchNo(cargo.getBatchNo());
        info.setCargoName(cargo.getCargoName());
        info.setCargoType(cargo.getCargoType());
        info.setOrigin(cargo.getOrigin());
        info.setDestination(cargo.getDestination());
        info.setQuantity(cargo.getQuantity());
        info.setUnit(cargo.getUnit());
        info.setWeight(cargo.getWeight());
        info.setRequiredMinTemp(cargo.getRequiredMinTemp());
        info.setRequiredMaxTemp(cargo.getRequiredMaxTemp());
        info.setStatus(cargo.getStatus());
        info.setLoadingTime(cargo.getLoadingTime());
        info.setExpectedArrivalTime(cargo.getExpectedArrivalTime());

        TemperatureZone zone = cargo.getTemperatureZone();
        if (zone != null) {
            info.setZoneCode(zone.getZoneCode());
            info.setZoneName(zone.getZoneName());
            if (zone.getVehicle() != null) {
                info.setVehiclePlate(zone.getVehicle().getPlateNumber());
            }
        }
        return info;
    }
}
