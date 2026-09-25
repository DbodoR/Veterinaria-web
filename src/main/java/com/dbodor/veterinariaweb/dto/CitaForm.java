package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos del formulario de agendamiento (HU-25).
 *
 * El cliente no se guarda en la cita: solo sirve para filtrar las mascotas
 * que se ofrecen. La cita queda asociada a la mascota, y por medio de ella
 * al dueno.
 */
@Getter
@Setter
public class CitaForm {

    private Long idCliente;

    @NotNull(message = "Seleccione la mascota")
    private Long idMascota;

    @NotNull(message = "Seleccione el servicio")
    private Long idServicio;

    @NotNull(message = "Seleccione el veterinario")
    private Long idVeterinario;

    @NotNull(message = "Indique la fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Indique la hora")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;
}