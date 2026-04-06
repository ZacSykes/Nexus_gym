package com.proyecto.nexus.usuario.repository;

import com.proyecto.nexus.usuario.model.Gimnasio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GimnasioRepository extends JpaRepository<Gimnasio, Long> {
}