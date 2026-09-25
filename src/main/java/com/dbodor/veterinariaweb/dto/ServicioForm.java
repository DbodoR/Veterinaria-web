package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Datos del formulario de servicios (HU-16).
 *
 * Las restricciones de duracion corresponden a HU-20: bloques de quince
 * minutos, entre quince y ciento ochenta. Se validan aqui para que la
 * agenda no pueda recibir franjas imposibles de asignar.
 */
@Getter
@Setter
public class ServicioForm {

    private Long idServicio;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Size(max = 400, message = "La descripcion no puede superar los 400 caracteres")
    private String descripcion;

    @NotNull(message = "La duracion es obligatoria")
    @Min(value = 15, message = "La duracion minima es de 15 minutos")
    @Max(value = 180, message = "La duracion maxima es de 180 minutos")
    private Integer duracionMinutos;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    private BigDecimal precioBase;

    private Boolean esConsultaVeterinaria;

    /**
     * HU-20: la duracion debe caer en bloques de quince minutos.
     * Se expresa como metodo @AssertTrue porque no existe una anotacion
     * estandar para "multiplo de".
     */
    @AssertTrue(message = "La duracion debe ser multiplo de 15 minutos")
    public boolean isDuracionEnBloquesValidos() {
        return duracionMinutos == null || duracionMinutos % 15 == 0;
    }
}