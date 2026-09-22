package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.security.RutasPorRol;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pantallas de inicio de cliente y veterinario. Por ahora son
 * marcadores de posicion: las llenan HU-13 y HU-09.
 */
@Controller
public class InicioController {

    @GetMapping("/")
    public String raiz(Authentication auth) {
        return "redirect:" + RutasPorRol.inicioDe(auth);
    }

    @GetMapping("/cliente/citas")
    public String citasDelCliente() {
        return "cliente/citas";
    }

    @GetMapping("/veterinario/agenda")
    public String agendaDelVeterinario() {
        return "veterinario/agenda";
    }
}