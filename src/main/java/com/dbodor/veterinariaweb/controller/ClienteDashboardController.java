package com.dbodor.veterinariaweb.controller;


import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.security.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteDashboardController {

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;

    public ClienteDashboardController(MascotaRepository mascotaRepository,
                                      CitaRepository citaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
    }

    @GetMapping({"", "/dashboard", "/citas", "/mascotas", "/cuenta"})
    public String dashboard(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
                            @RequestParam(value = "seccion", required = false, defaultValue = "mascotas") String seccion,
                            Model model) {

        Usuario usuario = usuarioAutenticado.getUsuario();
        Long idUsuario = usuario.getIdUsuario();

        // 1. Obtener mascotas del cliente usando el método existente porDuenoYEstado
        List<Mascota> mascotasActivas = mascotaRepository.porDuenoYEstado(idUsuario, "ACTIVO");

        // 2. Obtener historial de citas del cliente
        List<Cita> citas = citaRepository.listarPorClienteConDetalle(idUsuario);

        // 3. Proxima cita programada (filtrado en memoria)
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

        // 4. Inyectar datos al modelo Thymeleaf
        model.addAttribute("usuario", usuario);
        model.addAttribute("mascotas", mascotasActivas);
        model.addAttribute("citas", citas);
        model.addAttribute("totalMascotas", mascotasActivas.size());
        model.addAttribute("proximaCita", proximaCita);
        model.addAttribute("seccion", seccion);

        return "cliente/dashboard-user";
    }

}
