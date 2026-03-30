package com.Proyecto.Nexus.service;

import com.Proyecto.Nexus.domain.Usuarios;
import java.util.List;

public interface IusuariosService {

    List<Usuarios> listaUsuarios();

    Usuarios buscarUsuarioPorId(Long id);

    void guardarUsuario(Usuarios usuario);

    void eliminarUsuario(Long id);
}