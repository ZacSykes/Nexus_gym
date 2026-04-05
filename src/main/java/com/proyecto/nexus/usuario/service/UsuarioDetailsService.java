package com.proyecto.nexus.usuario.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.repository.PerfilRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private PerfilRepository perfilRepository;

@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Perfil perfil = perfilRepository.findByNombreUsuario(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    return new User(
        perfil.getNombreUsuario(),
        perfil.getContrasena(),
        List.of(new SimpleGrantedAuthority(perfil.getRol().getNombre()))
    );
}
}