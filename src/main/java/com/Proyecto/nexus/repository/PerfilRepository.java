package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
    Optional<Perfil> findByNombreUsuario(String nombreUsuario);
}