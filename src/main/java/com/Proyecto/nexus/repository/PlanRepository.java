package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByEstado(String estado);
}