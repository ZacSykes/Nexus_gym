package com.proyecto.nexus.clase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.clase.model.Clase;

public interface ClaseRepository extends JpaRepository<Clase, Integer> {
    List<Clase> findByDisponibleTrue();
    List<Clase> findByDisciplinaAndDisponibleTrue(String disciplina);
    List<Clase> findByNivelAndDisponibleTrue(String nivel);
}