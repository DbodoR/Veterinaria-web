package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.service.HistoriaClinicaService;
import com.dbodor.veterinariaweb.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reportes del panel de administracion.
 *   /citas      HU-26 citas e ingresos por periodo
 *   /historias  HU-24 historia clinica en PDF
 */
@Controller
@RequestMapping("/admin/reportes")
public class AdminReporteController {

    private final ReporteService reporteService;
    private final HistoriaClinicaService historiaClinicaService;

    public AdminReporteController(ReporteService reporteService,
                                  HistoriaClinicaService historiaClinicaService) {
        this.reporteService = reporteService;
        this.historiaClinicaService = historiaClinicaService;
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

    /** HU-24 CA1: seleccion de la mascota. */
    @GetMapping("/historias")
    public String historias(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("mascotas", historiaClinicaService.buscarMascotas(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return "admin/reportes/historias";
    }

    /** HU-24 CA2 a CA4 y CA6: historia en pantalla. */
    @GetMapping("/historias/{id}")
    public String historia(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Mascota> mascota = historiaClinicaService.mascota(id);
        if (mascota.isEmpty()) {
            flash.addFlashAttribute("mensajeError", "La mascota solicitada no existe.");
            return "redirect:/admin/reportes/historias";
        }
        model.addAttribute("mascota", mascota.get());
        model.addAttribute("atenciones", historiaClinicaService.atenciones(id));
        return "admin/reportes/historia-detalle";
    }

    /** HU-24 CA5: descarga en PDF. */
    @GetMapping("/historias/{id}/pdf")
    public ResponseEntity<byte[]> historiaPdf(@PathVariable Long id) {
        Optional<Mascota> mascota = historiaClinicaService.mascota(id);
        if (mascota.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Cita> atenciones = historiaClinicaService.atenciones(id);
        byte[] pdf = historiaClinicaService.generarPdf(mascota.get(), atenciones);

        String nombre = mascota.get().getNombre().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"historia-clinica-" + nombre + "-" + id + ".pdf\"")
                .body(pdf);
    }
}