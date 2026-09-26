package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Reportes del panel de administracion.
 *   /citas      HU-26 citas e ingresos por periodo
 *   /historias  HU-24 historia clinica (pendiente)
 */
@Controller
@RequestMapping("/admin/reportes")
public class AdminReporteController {

    private final ReporteService reporteService;

    public AdminReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/citas")
    public String citas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        @RequestParam(required = false) Long idVeterinario,
                        Model model) {

        model.addAttribute("veterinarios", reporteService.todosLosVeterinarios());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("idVeterinario", idVeterinario);

        if (desde == null && hasta == null) {
            return "admin/reportes/citas";
        }
        if (desde == null || hasta == null) {
            model.addAttribute("error", "Indique la fecha inicial y la final");
            return "admin/reportes/citas";
        }

        try {
            model.addAttribute("reporte", reporteService.citasEIngresos(desde, hasta, idVeterinario));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "admin/reportes/citas";
    }

    /** HU-24: el enlace del panel lleva aqui mientras el reporte no exista. */
    @GetMapping("/historias")
    public String historias() {
        return "admin/reportes/historias";
    }
}