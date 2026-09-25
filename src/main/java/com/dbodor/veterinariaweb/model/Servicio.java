package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio que presta la clinica.
 *
 * Cambios respecto de la version inicial, con la HU que los exige:
 *   unicidad de nombre   HU-16 criterio 2
 *   precioBase decimal   importes monetarios sin error de redondeo
 *   estado como enum     evita valores libres como "activo" o "Activo"
 *   fechaModificacion    HU-17 registro del cambio de precio
 *   usuarioModificacion  HU-17 quien realizo el cambio
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

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "es_consulta_veterinaria")
    private Boolean esConsultaVeterinaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
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