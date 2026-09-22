package com.dbodor.veterinariaweb.security;

import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HU-02 CA1: lleva a cada rol a su pantalla de inicio.
 * HU-02 CA4: un ingreso correcto reinicia el contador de intentos fallidos.
 * HU-05:     si la cuenta esta marcada para cambio de clave, va primero alli.
 */
@Component
public class ExitoAutenticacionHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;

    public ExitoAutenticacionHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .ifPresent(this::reiniciarIntentos);

        boolean debeCambiar = authentication.getPrincipal() instanceof UsuarioAutenticado ua
                && ua.debeCambiarPassword();

        String destino = debeCambiar ? "/cambiar-password" : RutasPorRol.inicioDe(authentication);
        getRedirectStrategy().sendRedirect(request, response, destino);
    }

    private void reiniciarIntentos(Usuario usuario) {
        if (usuario.getIntentosFallidos() != 0 || usuario.getBloqueadoHasta() != null) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);
        }
    }
}