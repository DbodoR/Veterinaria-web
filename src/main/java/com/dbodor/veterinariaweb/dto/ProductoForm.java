package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos del formulario de alta de productos (HU-22).
 *
 * Repite en el formulario la regla de CA2 que ya aplica el servicio, para
 * que el error aparezca junto al campo y no como un mensaje general.
 */
@Getter
@Setter
public class ProductoForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "La presentacion es obligatoria")
    @Size(max = 120, message = "La presentacion no puede superar los 120 caracteres")
    private String presentacion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    private Double precioUnitario;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private Boolean esRecetableVeterinario;
}