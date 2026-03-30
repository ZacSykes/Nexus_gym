package com.Proyecto.Nexus.dao;

import com.Proyecto.Nexus.domain.Usuarios;
import org.springframework.data.repository.CrudRepository;

public interface UsuariosDao extends CrudRepository<Usuarios, Long> {
}