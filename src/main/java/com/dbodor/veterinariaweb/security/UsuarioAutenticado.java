package com.dbodor.veterinariaweb.security;

import com.dbodor.veterinariaweb.model.EstadoUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Adapta la entidad Usuario a lo que Spring Security espera.
 *
 * Los metodos de estado traducen criterios de HU-02:
 *   isEnabled()          -> "la cuenta esta inactiva" (CA3)
 *   isAccountNonLocked() -> "bloqueo tras cinco intentos" (CA4)
 */
public class UsuarioAutenticado implements UserDetails {

    private final Usuario usuario;

    public UsuarioAutenticado(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getIdUsuario() {
        return usuario.getIdUsuario();
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public boolean debeCambiarPassword() {
        return Boolean.TRUE.equals(usuario.getDebeCambiarPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    /** Spring Security llama "username" al identificador; aqui es el correo. */
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonLocked() {
        LocalDateTime hasta = usuario.getBloqueadoHasta();
        return hasta == null || hasta.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return usuario.getEstado() == EstadoUsuario.ACTIVO;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}