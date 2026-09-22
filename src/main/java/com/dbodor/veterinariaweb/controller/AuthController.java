package com.dbodor.veterinariaweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    /**
     * Pantalla de acceso. El parametro "error" decide el mensaje,
     * segun los criterios CA2, CA3 y CA4 de HU-02.
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("mensajeError", switch (error) {
                case "inactiva"  -> "Tu cuenta no esta activa. Comunicate con la veterinaria.";
                case "bloqueada" -> "La cuenta esta bloqueada temporalmente por intentos fallidos. "
                                    + "Intenta de nuevo en unos minutos.";
                default          -> "Correo o contrasena incorrectos.";
            });
        }
        if (logout != null) {
            model.addAttribute("mensajeExito", "Cerraste sesion correctamente.");
        }
        return "login";
    }
}