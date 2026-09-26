package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.security.RutasPorRol;
import com.dbodor.veterinariaweb.security.UsuarioAutenticado;
import com.dbodor.veterinariaweb.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CambiarPasswordController {

    private final ClienteService clienteService;

    public CambiarPasswordController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/cambiar-password")
    public String vistaCambiarPassword(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                                       Authentication auth,
                                       Model model) {
        // Si no debe cambiar la contraseña, redirigir a su pantalla de inicio habitual
        if (!usuarioAutenticado.debeCambiarPassword()) {
            return "redirect:" + RutasPorRol.inicioDe(auth);
        }

        model.addAttribute("nombreUsuario", usuarioAutenticado.getNombre());
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String procesarCambioPassword(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                                         Authentication auth,
                                         @RequestParam("passwordActual") String passwordActual,
                                         @RequestParam("passwordNueva") String passwordNueva,
                                         @RequestParam("confirmacionPassword") String confirmacionPassword,
                                         Model model,
                                         RedirectAttributes flash) {

        if (!passwordNueva.equals(confirmacionPassword)) {
            model.addAttribute("mensajeError", "La confirmación de la contraseña no coincide.");
            model.addAttribute("nombreUsuario", usuarioAutenticado.getNombre());
            return "cambiar-password";
        }

        try {
            clienteService.cambiarPassword(usuarioAutenticado.getIdUsuario(), passwordActual, passwordNueva);
            // Actualizar la entidad en memoria para reflejar que ya no debe cambiarla
            usuarioAutenticado.getUsuario().setDebeCambiarPassword(Boolean.FALSE);

            flash.addFlashAttribute("mensajeExito", "Tu contraseña ha sido actualizada con éxito.");
            return "redirect:" + RutasPorRol.inicioDe(auth);
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("nombreUsuario", usuarioAutenticado.getNombre());
            return "cambiar-password";
        }
    }
}