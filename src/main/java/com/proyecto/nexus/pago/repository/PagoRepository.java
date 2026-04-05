package com.proyecto.nexus.pago.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.pago.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
}