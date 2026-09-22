package com.dbodor.veterinariaweb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HU-21 CA4: un cliente o un veterinario que abre la direccion del panel
 * no ve un error 403, sino que vuelve a su propia pantalla de inicio.
 */
@Component
public class AccesoDenegadoHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        response.sendRedirect(request.getContextPath() + RutasPorRol.inicioDe(auth));
    }
}