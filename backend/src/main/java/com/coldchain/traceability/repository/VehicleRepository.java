package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    @Query("SELECT v FROM Vehicle v JOIN FETCH v.temperatureZones WHERE v.status = 'TRANSIT'")
    List<Vehicle> findAllActiveVehiclesWithZones();

    @Query("SELECT v FROM Vehicle v JOIN FETCH v.temperatureZones WHERE v.plateNumber = :plateNumber")
    Optional<Vehicle> findByPlateNumberWithZones(String plateNumber);
}
