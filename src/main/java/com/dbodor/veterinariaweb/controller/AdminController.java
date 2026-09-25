package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

/**
 * HU-21 Panel de administracion.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminController(CitaRepository citaRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** CA3: resumen con citas del dia, citas pendientes y clientes activos. */
    @GetMapping
    public String panel(Model model) {
        model.addAttribute("citasHoy", citaRepository.countByFechaCita(LocalDate.now()));
        model.addAttribute("citasPendientes", citaRepository.countByEstado("PROGRAMADA"));
        model.addAttribute("clientesActivos",
                usuarioRepository.countByRolAndEstado(RolUsuario.CLIENTE, EstadoUsuario.ACTIVO));
        return "admin/panel";
    }
}