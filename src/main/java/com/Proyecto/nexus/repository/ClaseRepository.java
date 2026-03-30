package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaseRepository extends JpaRepository<Clase, Integer> {
    List<Clase> findByDisponibleTrue();
    List<Clase> findByDisciplinaAndDisponibleTrue(String disciplina);
    List<Clase> findByNivelAndDisponibleTrue(String nivel);
}