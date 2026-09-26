package com.dbodor.veterinariaweb.dto;

import com.dbodor.veterinariaweb.model.Cita;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Resultado del reporte de citas e ingresos (HU-26).
 * Las citas del periodo mas el resumen que pide CA3.
 */
@Getter
@AllArgsConstructor
public class ReporteCitas {

    private final List<Cita> citas;
    private final long total;
    private final long finalizadas;
    private final long canceladas;

    /** Suma del valor de las citas finalizadas; las demas no generan ingreso. */
    private final double ingresos;
}