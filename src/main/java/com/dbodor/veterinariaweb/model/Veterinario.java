package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "veterinarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVeterinario;

    private String nombre;
    private String cedula;

    @Column(name = "tarjeta_profesional")
    private String tarjetaProfesional;
    private String especialidad;
    private String correo;
    private String telefono;
    private String estado;

}
