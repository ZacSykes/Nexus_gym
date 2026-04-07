package com.proyecto.nexus.usuario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.usuario.model.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {
    List<Profesor> findAllByOrderByNombreAscApellidoAsc();
}
