package com.dbodor.veterinariaweb.security;

import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .map(UsuarioAutenticado::new)
                // Spring Security convierte esta excepcion en BadCredentials, asi el
                // mensaje al usuario no revela si el correo existe (HU-02 CA2).
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
    }
}