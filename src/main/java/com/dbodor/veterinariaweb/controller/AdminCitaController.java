package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.CitaForm;
import com.dbodor.veterinariaweb.service.AgendaCitaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-25 Agendar una cita.
 * Pantalla transaccional del panel de administracion.
 */
@Controller
@RequestMapping("/admin/citas")
public class AdminCitaController {

    private static final String VISTA_LISTA = "admin/citas/lista";
    private static final String VISTA_FORMULARIO = "admin/citas/formulario";

    private final AgendaCitaService agendaCitaService;

    public AdminCitaController(AgendaCitaService agendaCitaService) {
        this.agendaCitaService = agendaCitaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", agendaCitaService.listar());
        return VISTA_LISTA;
    }

    /**
     * Primer paso: se elige el cliente y la misma pantalla se recarga con
     * sus mascotas activas (CA2). No requiere JavaScript.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long idCliente, Model model) {
        CitaForm form = new CitaForm();
        form.setIdCliente(idCliente);
        model.addAttribute("citaForm", form);
        cargarOpciones(model, idCliente);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String agendar(@Valid @ModelAttribute CitaForm citaForm,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash) {

        if (citaForm.getIdCliente() == null) {
            citaForm.setIdCliente(agendaCitaService.duenoDeMascota(citaForm.getIdMascota()));
        }

        if (errores.hasErrors()) {
            cargarOpciones(model, citaForm.getIdCliente());
            return VISTA_FORMULARIO;
        }

        try {
            agendaCitaService.agendar(citaForm);
            flash.addFlashAttribute("mensajeExito", "Cita agendada correctamente.");
            return "redirect:/admin/citas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            cargarOpciones(model, citaForm.getIdCliente());
            return VISTA_FORMULARIO;
        }
    }

    private void cargarOpciones(Model model, Long idCliente) {
        model.addAttribute("clientes", agendaCitaService.clientesActivos());
        model.addAttribute("mascotas", agendaCitaService.mascotasActivas(idCliente));
        model.addAttribute("servicios", agendaCitaService.serviciosActivos());
        model.addAttribute("veterinarios", agendaCitaService.veterinariosActivos());
    }
}