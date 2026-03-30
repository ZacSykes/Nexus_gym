package com.Proyecto.nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.Proyecto.nexus.repository.PerfilRepository;
import com.Proyecto.nexus.model.Perfil;
import java.util.List;

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
            perfil.getContraseña(),
            List.of(new SimpleGrantedAuthority("ROLE_" + perfil.getRol().getNombre().toUpperCase()))
        );
    }
}