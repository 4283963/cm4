package com.coldchain.traceability.repository;

import com.coldchain.traceability.entity.ElectronicFence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectronicFenceRepository extends JpaRepository<ElectronicFence, Long> {

    List<ElectronicFence> findByEnabledTrue();

    @Query("SELECT f FROM ElectronicFence f WHERE f.enabled = true " +
           "AND f.fenceType = 'POLYGON' " +
           "AND f.coordinates IS NOT NULL")
    List<ElectronicFence> findActivePolygonFences();
}
