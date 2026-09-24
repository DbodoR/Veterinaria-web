package com.dbodor.veterinariaweb.model;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "servicios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    private String nombre;
    private String descripcion;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "precio_base")
    private Double precioBase;

    @Column(name = "es_consulta_veterinaria")
    private Boolean esConsultaVeterinaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoServicio estado;

}
