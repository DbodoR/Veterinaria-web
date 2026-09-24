package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.ClienteForm;
import com.dbodor.veterinariaweb.service.ClienteService;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-01 Registrar un cliente nuevo.
 * Maestro de clientes del panel de administracion.
 */
@Controller
@RequestMapping("/admin/clientes")
public class AdminClienteController {

    private static final String VISTA_LISTA = "admin/clientes/lista";
    private static final String VISTA_FORMULARIO = "admin/clientes/formulario";

    private final ClienteService clienteService;

    public AdminClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /** Listado con busqueda por nombre o documento. */
    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("clientes", clienteService.listar(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return VISTA_LISTA;
    }

    /** Formulario de alta. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("clienteForm", new ClienteForm());
        return VISTA_FORMULARIO;
    }

    /**
     * Guarda el cliente.
     *
     * Las validaciones de campo obligatorio las resuelve @Valid antes de
     * entrar al metodo; las de duplicidad las devuelve el servicio como
     * excepcion y se convierten en un error del campo correspondiente.
     */
    @PostMapping
    public String guardar(@Valid @ModelAttribute ClienteForm clienteForm,
                          BindingResult errores,
                          RedirectAttributes flash) {

        if (errores.hasErrors()) {
            return VISTA_FORMULARIO;
        }

        try {
            ClienteService.AltaCliente alta = clienteService.registrar(clienteForm);
            flash.addFlashAttribute("mensajeExito",
                    "Cliente " + alta.cliente().getNombre() + " registrado correctamente.");
            flash.addFlashAttribute("passwordTemporal", alta.passwordTemporal());
            flash.addFlashAttribute("correoNuevo", alta.cliente().getCorreo());
            return "redirect:/admin/clientes";
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }
}