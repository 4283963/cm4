package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.CargoBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoBatchRepository extends JpaRepository<CargoBatch, Long> {

    Optional<CargoBatch> findByBatchNo(String batchNo);

    @Query("SELECT c FROM CargoBatch c JOIN FETCH c.temperatureZone tz JOIN FETCH tz.vehicle v WHERE c.batchNo = :batchNo")
    Optional<CargoBatch> findByBatchNoWithDetails(String batchNo);

    @Query("SELECT c FROM CargoBatch c JOIN FETCH c.temperatureZone tz WHERE tz.vehicle.plateNumber = :plateNumber AND c.status = 'IN_TRANSIT'")
    List<CargoBatch> findInTransitByVehiclePlate(String plateNumber);

    @Query("SELECT c FROM CargoBatch c JOIN FETCH c.temperatureZone tz JOIN FETCH tz.vehicle v WHERE c.status = 'IN_TRANSIT'")
    List<CargoBatch> findAllInTransitWithDetails();
}
