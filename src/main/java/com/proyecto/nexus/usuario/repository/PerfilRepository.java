package com.proyecto.nexus.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.model.DatosUsuario;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByNombreUsuario(String nombreUsuario);

    Optional<Perfil> findByUsuario(DatosUsuario usuario); // 🔥 ESTE FALTABA
}