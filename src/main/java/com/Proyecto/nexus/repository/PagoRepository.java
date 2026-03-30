package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
}