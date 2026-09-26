package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.security.RutasPorRol;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pantallas de inicio de cliente y veterinario. Por ahora son
 * marcadores de posicion: las llenan HU-13 y HU-09.
 */
@Controller
public class InicioController {

    @GetMapping({"/", "/home"})
    public String raiz(Authentication auth) {
        // Si el usuario SÍ está autenticado (y no es anónimo), lo mandamos a su panel
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:" + RutasPorRol.inicioDe(auth);
        }

        return "home";
    }

    @GetMapping("/cliente/citas")
    public String citasDelCliente() {
        return "redirect:/cliente/dashboard?seccion=citas";
    }

    @GetMapping("/veterinario/agenda")
    public String agendaDelVeterinario() {
        return "veterinario/agenda";
    }
}