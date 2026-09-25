package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.ServicioForm;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-16 Crear un servicio.
 * Maestro de servicios del panel de administracion.
 */
@Controller
@RequestMapping("/admin/servicios")
public class AdminServicioController {

    private static final String VISTA_LISTA = "admin/servicios/lista";
    private static final String VISTA_FORMULARIO = "admin/servicios/formulario";

    private final ServicioService servicioService;

    public AdminServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("servicios", servicioService.buscar(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("servicioForm", new ServicioForm());
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute ServicioForm servicioForm,
                          BindingResult errores,
                          RedirectAttributes flash) {

        if (errores.hasErrors()) {
            return VISTA_FORMULARIO;
        }

        try {
            Servicio creado = servicioService.crear(servicioForm);
            flash.addFlashAttribute("mensajeExito",
                    "Servicio " + creado.getNombre() + " creado correctamente.");
            return "redirect:/admin/servicios";
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }
}