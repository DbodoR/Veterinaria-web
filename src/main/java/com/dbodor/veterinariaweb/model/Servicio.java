package com.dbodor.veterinariaweb.model;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Servicio que presta la clinica.
 *
 * Version unificada de HU-16, HU-19 y HU-20. Respecto de la version
 * original se agregaron:
 *   unicidad de nombre                HU-16 criterio 2
 *   fechaModificacion / usuario       HU-17 registro del cambio de precio
 *   @PrePersist con valores por omision
 *   estaActivo() como atajo para las vistas
 *
 * precioBase se mantiene como Double para no romper el codigo ya escrito
 * de HU-19 y HU-20.
 */
@Entity
@Table(
        name = "servicios",
        uniqueConstraints = @UniqueConstraint(name = "uk_servicios_nombre", columnNames = "nombre")
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Servicio {

    /** Bloque minimo de agenda, en minutos (HU-20). */
    public static final int BLOQUE_MINUTOS = 15;
    public static final int DURACION_MINIMA = 15;
    public static final int DURACION_MAXIMA = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 400)
    private String descripcion;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(name = "es_consulta_veterinaria")
    private Boolean esConsultaVeterinaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoServicio estado;

    /** HU-17: queda registro de quien cambio el precio y cuando. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificacion", length = 150)
    private String usuarioModificacion;

    @PrePersist
    protected void alCrear() {
        if (estado == null) {
            estado = EstadoServicio.ACTIVO;
        }
        if (esConsultaVeterinaria == null) {
            esConsultaVeterinaria = Boolean.FALSE;
        }
    }

    public boolean estaActivo() {
        return estado == EstadoServicio.ACTIVO;
    }
}