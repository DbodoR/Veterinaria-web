package com.dbodor.veterinariaweb.security;

import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * HU-02 CA2: un unico mensaje para correo inexistente y clave incorrecta.
 * HU-02 CA3: motivo distinto cuando la cuenta esta inactiva.
 * HU-02 CA4: cuenta los intentos y bloquea al quinto.
 */
@Component
public class FalloAutenticacionHandler extends SimpleUrlAuthenticationFailureHandler {

    /** Intentos consecutivos permitidos antes del bloqueo. */
    public static final int MAX_INTENTOS = 5;

    /** Duracion del bloqueo temporal. */
    public static final int MINUTOS_BLOQUEO = 15;

    private final UsuarioRepository usuarioRepository;

    public FalloAutenticacionHandler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String destino;

        if (exception instanceof DisabledException) {
            destino = "/login?error=inactiva";
        } else if (exception instanceof LockedException) {
            destino = "/login?error=bloqueada";
        } else {
            registrarIntentoFallido(request.getParameter("username"));
            destino = "/login?error=credenciales";
        }

        getRedirectStrategy().sendRedirect(request, response, destino);
    }

    /**
     * Solo suma si el correo corresponde a una cuenta real. Un correo
     * inexistente no crea nada ni cambia el mensaje devuelto.
     */
    private void registrarIntentoFallido(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(usuario -> {
            int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
            intentos++;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= MAX_INTENTOS) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            }
            usuarioRepository.save(usuario);
        });
    }
}