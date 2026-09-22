package com.dbodor.veterinariaweb.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Un solo lugar que decide a donde va cada rol. Lo usan el handler de
 * login exitoso y el de acceso denegado, para que HU-02 CA1 y HU-21 CA4
 * no se contradigan.
 */
public final class RutasPorRol {

    public static final String PANEL_ADMIN = "/admin";
    public static final String INICIO_CLIENTE = "/cliente/citas";
    public static final String INICIO_VETERINARIO = "/veterinario/agenda";
    public static final String LOGIN = "/login";

    private RutasPorRol() {
    }

    public static String inicioDe(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return LOGIN;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            switch (a.getAuthority()) {
                case "ROLE_ADMINISTRADOR": return PANEL_ADMIN;
                case "ROLE_VETERINARIO":   return INICIO_VETERINARIO;
                case "ROLE_CLIENTE":       return INICIO_CLIENTE;
                default: break;
            }
        }
        return LOGIN;
    }
}
