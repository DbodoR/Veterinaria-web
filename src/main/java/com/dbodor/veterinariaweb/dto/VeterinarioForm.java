package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Datos del formulario de alta de veterinarios (HU-06).
 *
 * Reune los datos personales, que se guardaran en Usuario, y los
 * profesionales, que se guardaran en Veterinario.
 */
@Getter
@Setter
public class VeterinarioForm {

    private Long idVeterinario;

    // ---- Datos personales (van a Usuario) ----

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 30, message = "El documento no puede superar los 30 caracteres")
    private String documento;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String correo;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 30, message = "El telefono no puede superar los 30 caracteres")
    private String telefono;

    // ---- Datos profesionales (van a Veterinario) ----

    @NotBlank(message = "La tarjeta profesional es obligatoria")
    @Size(max = 50, message = "La tarjeta profesional no puede superar los 50 caracteres")
    private String tarjetaProfesional;

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 80, message = "La especialidad no puede superar los 80 caracteres")
    private String especialidad;

    @NotNull(message = "La tarifa es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa debe ser mayor que cero")
    private BigDecimal tarifa;

    /** Opcionales: si vienen vacios se aplica la jornada por defecto. */
    private LocalTime horaInicio;

    private LocalTime horaFin;
}