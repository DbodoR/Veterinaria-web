package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.MascotaForm;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-11 Registrar una mascota y HU-12 Consultar las mascotas registradas.
 * Maestro de mascotas del panel de administracion.
 */
@Controller
@RequestMapping("/admin/mascotas")
public class AdminMascotaController {

    private static final String VISTA_LISTA = "admin/mascotas/lista";
    private static final String VISTA_FORMULARIO = "admin/mascotas/formulario";

    private final MascotaService mascotaService;

    public AdminMascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    /** HU-12: listado con busqueda por mascota, dueno o documento. */
    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("mascotas", mascotaService.buscar(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return VISTA_LISTA;
    }

    /** HU-11: formulario de alta. */
    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("mascotaForm", new MascotaForm());
        cargarOpciones(model);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute MascotaForm mascotaForm,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash) {

        if (errores.hasErrors()) {
            cargarOpciones(model);
            return VISTA_FORMULARIO;
        }

        try {
            Mascota mascota = mascotaService.registrar(mascotaForm);
            flash.addFlashAttribute("mensajeExito",
                    "Mascota " + mascota.getNombre() + " registrada correctamente.");
            return "redirect:/admin/mascotas";
        } catch (IllegalArgumentException e) {
            errores.rejectValue("idCliente", "duenoInvalido", e.getMessage());
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
        }

        cargarOpciones(model);
        return VISTA_FORMULARIO;
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("clientes", mascotaService.clientesActivos());
        model.addAttribute("especies", MascotaForm.ESPECIES);
    }
}