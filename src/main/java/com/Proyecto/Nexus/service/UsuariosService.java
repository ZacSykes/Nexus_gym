package com.Proyecto.Nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.Proyecto.Nexus.domain.Usuarios;
import com.Proyecto.Nexus.dao.UsuariosDao;
import java.util.List;

@Service
public class UsuariosService implements IusuariosService {

    @Autowired
    private UsuariosDao usuariosDao;

    @Override
    public List<Usuarios> listaUsuarios() {
        return (List<Usuarios>) usuariosDao.findAll();
    }

    @Override
    public Usuarios buscarUsuarioPorId(Long id) {
        return usuariosDao.findById(id).orElse(null);
    }

    @Override
    public void guardarUsuario(Usuarios usuario) {
        usuariosDao.save(usuario); // 🔥 AQUÍ GUARDA
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuariosDao.deleteById(id);
    }
}