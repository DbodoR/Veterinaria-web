package com.dbodor.veterinariaweb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PerfilClienteDto {

    private Long idUsuario;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombre;

    private String documento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo electrónico válido")
    private String correo;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefono;

    private String direccion;
    private String ciudad;

}
