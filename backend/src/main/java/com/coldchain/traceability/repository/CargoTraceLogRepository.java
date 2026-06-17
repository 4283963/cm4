package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.CargoTraceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CargoTraceLogRepository extends JpaRepository<CargoTraceLog, Long> {

    @Query("SELECT t FROM CargoTraceLog t WHERE t.cargoBatch.batchNo = :batchNo ORDER BY t.traceTime ASC")
    List<CargoTraceLog> findByBatchNoOrderByTraceTime(@Param("batchNo") String batchNo);

    @Query("SELECT t FROM CargoTraceLog t WHERE t.cargoBatch.batchNo = :batchNo AND t.traceTime BETWEEN :startTime AND :endTime ORDER BY t.traceTime ASC")
    List<CargoTraceLog> findByBatchNoAndTimeRange(
            @Param("batchNo") String batchNo,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT t FROM CargoTraceLog t WHERE t.vehiclePlate = :plateNumber AND t.traceTime >= :since ORDER BY t.traceTime DESC")
    List<CargoTraceLog> findByVehiclePlateSince(
            @Param("plateNumber") String plateNumber,
            @Param("since") LocalDateTime since);

    @Query("SELECT t FROM CargoTraceLog t WHERE t.temperatureStatus = 'ABNORMAL' AND t.traceTime >= :since ORDER BY t.traceTime DESC")
    List<CargoTraceLog> findRecentAbnormalLogs(@Param("since") LocalDateTime since);
}
