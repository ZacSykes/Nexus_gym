package com.proyecto.nexus.plan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.plan.model.Plan;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByEstado(String estado);
}