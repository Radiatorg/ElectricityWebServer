package com.verchuk.electro.repository;

import com.verchuk.electro.model.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, Long> {
    List<Appliance> findByActiveTrue();
    
    @Query("SELECT a FROM Appliance a WHERE a.active = true ORDER BY a.name")
    List<Appliance> findAllActiveOrderedByName();
}

