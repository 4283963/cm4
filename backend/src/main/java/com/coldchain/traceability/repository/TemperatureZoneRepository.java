package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.TemperatureZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemperatureZoneRepository extends JpaRepository<TemperatureZone, Long> {

    @Query("SELECT tz FROM TemperatureZone tz JOIN FETCH tz.vehicle WHERE tz.vehicle.plateNumber = :plateNumber")
    List<TemperatureZone> findByVehiclePlate(@Param("plateNumber") String plateNumber);

    @Query("SELECT tz FROM TemperatureZone tz JOIN FETCH tz.vehicle WHERE tz.vehicle.plateNumber = :plateNumber AND tz.zoneCode = :zoneCode")
    Optional<TemperatureZone> findByVehiclePlateAndZoneCode(
            @Param("plateNumber") String plateNumber,
            @Param("zoneCode") String zoneCode);

    @Query("SELECT tz FROM TemperatureZone tz JOIN FETCH tz.cargoBatches WHERE tz.id = :zoneId")
    Optional<TemperatureZone> findByIdWithCargos(@Param("zoneId") Long zoneId);
}
