package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.ReporteCitas;
import com.dbodor.veterinariaweb.model.Veterinario;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes del panel de administracion.
 *   HU-26  citas e ingresos por periodo y veterinario
 */
public interface ReporteService {

    String ESTADO_FINALIZADA = "FINALIZADA";
    String ESTADO_CANCELADA = "CANCELADA";

    /**
     * CA1 a CA3. Lanza IllegalArgumentException si la fecha inicial es
     * posterior a la final (CA5). idVeterinario nulo significa todos.
     */
    ReporteCitas citasEIngresos(LocalDate desde, LocalDate hasta, Long idVeterinario);

    /** CA4: todos los veterinarios, activos o no, para el filtro. */
    List<Veterinario> todosLosVeterinarios();
}