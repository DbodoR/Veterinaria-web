package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.VeterinarioForm;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.VeterinarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-06 Registrar un veterinario.
 * Maestro de veterinarios del panel de administracion.
 */
@Controller
@RequestMapping("/admin/veterinarios")
public class AdminVeterinarioController {

    private static final String VISTA_LISTA = "admin/veterinarios/lista";
    private static final String VISTA_FORMULARIO = "admin/veterinarios/formulario";

    private final VeterinarioService veterinarioService;

    public AdminVeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(required = false) String especialidad,
                         Model model) {
        model.addAttribute("veterinarios", veterinarioService.listar(buscar, especialidad));
        model.addAttribute("especialidades", veterinarioService.especialidades());
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        model.addAttribute("especialidadFiltro", especialidad == null ? "" : especialidad);
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("veterinarioForm", new VeterinarioForm());
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute VeterinarioForm veterinarioForm,
                          BindingResult errores,
                          RedirectAttributes flash) {

        if (errores.hasErrors()) {
            return VISTA_FORMULARIO;
        }

        try {
            VeterinarioService.AltaVeterinario alta = veterinarioService.registrar(veterinarioForm);
            flash.addFlashAttribute("mensajeExito",
                    "Veterinario " + alta.veterinario().getNombre() + " registrado correctamente.");
            flash.addFlashAttribute("passwordTemporal", alta.passwordTemporal());
            flash.addFlashAttribute("correoNuevo", alta.veterinario().getCorreo());
            return "redirect:/admin/veterinarios";
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }
}