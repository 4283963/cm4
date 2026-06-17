package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByAlertStatusOrderByCreatedAtDesc(String alertStatus);

    List<Alert> findByCargoBatchIdOrderByCreatedAtDesc(Long cargoBatchId);

    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    @Query("SELECT a FROM Alert a WHERE a.alertStatus = 'PENDING' ORDER BY a.createdAt DESC")
    List<Alert> findPendingAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.alertStatus = 'PENDING' AND a.alertLevel = 'CRITICAL'")
    Long countCriticalPendingAlerts();

    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findAlertsSince(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM Alert a WHERE a.cargoBatchId = :cargoBatchId AND a.alertType = 'TEMPERATURE_BROKEN_CHAIN' ORDER BY a.createdAt DESC")
    List<Alert> findBrokenChainAlertsForCargo(@Param("cargoBatchId") Long cargoBatchId);

    @Query("SELECT a FROM Alert a WHERE a.cargoBatchId = :cargoBatchId AND a.alertStatus = 'PENDING' ORDER BY a.createdAt DESC")
    Optional<Alert> findLatestPendingAlertForCargo(@Param("cargoBatchId") Long cargoBatchId);
}
