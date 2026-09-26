package com.dbodor.veterinariaweb.controller;


import com.dbodor.veterinariaweb.dto.CitaForm;
import com.dbodor.veterinariaweb.dto.MascotaForm;
import com.dbodor.veterinariaweb.dto.PerfilClienteDto;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.security.UsuarioAutenticado;
import com.dbodor.veterinariaweb.service.AgendaCitaService;
import com.dbodor.veterinariaweb.service.ClienteService;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteDashboardController {

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ClienteService clienteService;
    private final MascotaService mascotaService;
    private final AgendaCitaService agendaCitaService;

    public ClienteDashboardController(MascotaRepository mascotaRepository,
                                      CitaRepository citaRepository,
                                      ClienteService clienteService,
                                      MascotaService mascotaService,
                                      AgendaCitaService agendaCitaService) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
        this.agendaCitaService = agendaCitaService;
    }

    /**
     * Garantiza que mascotaForm SIEMPRE esté en el Model para evitar errores de parseo en Thymeleaf.
     */
    @ModelAttribute("mascotaForm")
    public MascotaForm predeterminarMascotaForm() {
        return new MascotaForm();
    }

    @ModelAttribute("citaForm")
    public CitaForm predeterminarCitaForm() {
        return new CitaForm();
    }

    /**
     * Garantiza que la lista de especies siempre esté presente para los selects.
     */
    @ModelAttribute("especies")
    public List<String> especiesDisponibles() {
        return MascotaForm.ESPECIES;
    }

    @GetMapping({"", "/dashboard", "/citas", "/mascotas", "/cuenta"})
    public String dashboard(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                            @RequestParam(value = "seccion", required = false, defaultValue = "mascotas") String seccion,
                            Model model) {

        Usuario usuario = usuarioAutenticado.getUsuario();
        Long idUsuario = usuario.getIdUsuario();

        cargarDatosDashboard(model, idUsuario, usuario, seccion);

        if (!model.containsAttribute("perfilForm")) {
            PerfilClienteDto perfil = clienteService.consultarPerfil(idUsuario);
            model.addAttribute("perfilForm", perfil);
        }

        return "dashboard-user";
    }

    /**
     * Agendar cita para una mascota del cliente autenticado
     */
    @PostMapping("/citas")
    public String agendarCita(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                              @Valid @ModelAttribute("citaForm") CitaForm citaForm,
                              BindingResult errores,
                              Model model,
                              RedirectAttributes flash) {

        Usuario usuario = usuarioAutenticado.getUsuario();
        Long idUsuario = usuario.getIdUsuario();

        citaForm.setIdCliente(idUsuario);

        boolean tieneErroresReales = errores.getFieldErrors().stream()
                .anyMatch(err -> !"idCliente".equals(err.getField()));

        if (tieneErroresReales) {
            cargarDatosDashboard(model, idUsuario, usuario, "citas");
            if (!model.containsAttribute("perfilForm")) {
                model.addAttribute("perfilForm", clienteService.consultarPerfil(idUsuario));
            }
            model.addAttribute("abrirModalCita", true);
            return "dashboard-user";
        }

        try {
            agendaCitaService.agendar(citaForm);
            flash.addFlashAttribute("mensajeExitoCita", "Cita agendada exitosamente.");
            return "redirect:/cliente/dashboard?seccion=citas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("mensajeErrorCita", e.getMessage());
            cargarDatosDashboard(model, idUsuario, usuario, "citas");
            if (!model.containsAttribute("perfilForm")) {
                model.addAttribute("perfilForm", clienteService.consultarPerfil(idUsuario));
            }
            model.addAttribute("abrirModalCita", true);
            return "dashboard-user";
        }
    }

    /**
     * HU-11: Registrar una mascota asociada automáticamente al cliente autenticado
     */
    @PostMapping("/mascotas")
    public String registrarMascota(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                                   @Valid @ModelAttribute("mascotaForm") MascotaForm mascotaForm,
                                   BindingResult errores,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario usuario = usuarioAutenticado.getUsuario();
        Long idUsuario = usuario.getIdUsuario();

        // 1. Asignar forzosamente el ID del cliente autenticado
        mascotaForm.setIdCliente(idUsuario);

        // 2. Verificar si existen errores reales en los campos de la mascota (nombre, fecha, peso, etc.)
        boolean tieneErroresReales = errores.getFieldErrors().stream()
                .anyMatch(err -> !"idCliente".equals(err.getField()));

        if (tieneErroresReales) {
            cargarDatosDashboard(model, idUsuario, usuario, "mascotas");
            if (!model.containsAttribute("perfilForm")) {
                model.addAttribute("perfilForm", clienteService.consultarPerfil(idUsuario));
            }
            model.addAttribute("abrirModalMascota", true);
            return "dashboard-user";
        }

        try {
            Mascota guardada = mascotaService.registrar(mascotaForm);
            flash.addFlashAttribute("mensajeExitoMascota", "Mascota " + guardada.getNombre() + " registrada correctamente.");
            return "redirect:/cliente/dashboard?seccion=mascotas";
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
            cargarDatosDashboard(model, idUsuario, usuario, "mascotas");
            if (!model.containsAttribute("perfilForm")) {
                model.addAttribute("perfilForm", clienteService.consultarPerfil(idUsuario));
            }
            model.addAttribute("abrirModalMascota", true);
            return "dashboard-user";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeErrorMascota", e.getMessage());
            cargarDatosDashboard(model, idUsuario, usuario, "mascotas");
            if (!model.containsAttribute("perfilForm")) {
                model.addAttribute("perfilForm", clienteService.consultarPerfil(idUsuario));
            }
            model.addAttribute("abrirModalMascota", true);
            return "dashboard-user";
        }
    }

    /**
     * HU-03: Actualizar perfil del cliente
     */
    @PostMapping("/cuenta")
    public String actualizarPerfil(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                                   @Valid @ModelAttribute("perfilForm") PerfilClienteDto perfilForm,
                                   BindingResult errores,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario usuario = usuarioAutenticado.getUsuario();
        Long idUsuario = usuario.getIdUsuario();

        if (errores.hasErrors()) {
            cargarDatosDashboard(model, idUsuario, usuario, "cuenta");
            return "dashboard-user";
        }

        try {
            PerfilClienteDto actualizado = clienteService.actualizarPerfil(idUsuario, perfilForm);
            // Actualizar datos en memoria de la sesión
            usuario.setNombre(actualizado.getNombre());
            usuario.setCorreo(actualizado.getCorreo());
            usuario.setTelefono(actualizado.getTelefono());
            usuario.setDireccion(actualizado.getDireccion());
            usuario.setCiudad(actualizado.getCiudad());

            flash.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
            return "redirect:/cliente/dashboard?seccion=cuenta";
        } catch (DatoDuplicadoException e) {
            errores.rejectValue(e.getCampo(), "duplicado", e.getMessage());
            cargarDatosDashboard(model, idUsuario, usuario, "cuenta");
            return "dashboard-user";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            cargarDatosDashboard(model, idUsuario, usuario, "cuenta");
            return "dashboard-user";
        }
    }

    /**
     * HU-05: Cambiar contraseña
     */
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                                  @RequestParam("passwordActual") String passwordActual,
                                  @RequestParam("passwordNueva") String passwordNueva,
                                  @RequestParam("confirmacionPassword") String confirmacionPassword,
                                  RedirectAttributes flash) {

        Long idUsuario = usuarioAutenticado.getIdUsuario();

        if (!passwordNueva.equals(confirmacionPassword)) {
            flash.addFlashAttribute("mensajeErrorPassword", "La confirmación de la contraseña no coincide.");
            return "redirect:/cliente/dashboard?seccion=cuenta";
        }

        try {
            clienteService.cambiarPassword(idUsuario, passwordActual, passwordNueva);
            usuarioAutenticado.getUsuario().setDebeCambiarPassword(Boolean.FALSE);
            flash.addFlashAttribute("mensajeExitoPassword", "Contraseña cambiada exitosamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeErrorPassword", e.getMessage());
        }

        return "redirect:/cliente/dashboard?seccion=cuenta";
    }

    private void cargarDatosDashboard(Model model, Long idUsuario, Usuario usuario, String seccion) {
        List<Mascota> mascotasActivas = mascotaRepository.porDuenoYEstado(idUsuario, "ACTIVO");
        List<Cita> citas = citaRepository.listarPorClienteConDetalle(idUsuario);

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        Cita proximaCita = citas.stream()
                .filter(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()))
                .filter(c -> c.getFechaCita().isAfter(hoy) ||
                        (c.getFechaCita().isEqual(hoy) && !c.getHoraCita().isBefore(ahora)))
                .min((c1, c2) -> {
                    int cmp = c1.getFechaCita().compareTo(c2.getFechaCita());
                    return (cmp != 0) ? cmp : c1.getHoraCita().compareTo(c2.getHoraCita());
                })
                .orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("mascotas", mascotasActivas);
        model.addAttribute("citas", citas);
        model.addAttribute("totalMascotas", mascotasActivas.size());
        model.addAttribute("proximaCita", proximaCita);
        model.addAttribute("seccion", seccion);

        // Opciones transaccionales para agendar citas
        model.addAttribute("serviciosDisponibles", agendaCitaService.serviciosActivos());
        model.addAttribute("veterinariosDisponibles", agendaCitaService.veterinariosActivos());
    }
}
