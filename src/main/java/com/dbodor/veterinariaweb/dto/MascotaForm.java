package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos del formulario de alta de mascotas (HU-11).
 *
 * Las anotaciones cubren CA3, CA4 y CA5: si la especie no esta en la lista,
 * la fecha es futura o el peso no es positivo, Spring devuelve el error junto
 * al campo y el servicio nunca se ejecuta.
 */
@Getter
@Setter
public class MascotaForm {

    /** CA3: unicas especies aceptadas, en el orden en que se ofrecen. */
    public static final List<String> ESPECIES = List.of("Perro", "Gato", "Ave", "Roedor", "Otro");

    @NotNull(message = "Seleccione el dueno")
    private Long idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Pattern(regexp = "Perro|Gato|Ave|Roedor|Otro", message = "Seleccione una especie de la lista")
    private String especie;

    @Size(max = 80, message = "La raza no puede superar los 80 caracteres")
    private String raza;

    @NotBlank(message = "El sexo es obligatorio")
    @Pattern(regexp = "M|H", message = "Seleccione macho o hembra")
    private String sexo;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor que cero")
    private Double pesoKg;
}