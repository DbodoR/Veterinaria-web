package com.dbodor.veterinariaweb.model;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/*
  Ficha profesional de un veterinario.

  Los datos personales (nombre, documento, correo, telefono) viven en
  Usuario y no se repiten aqui: asi el correo de acceso y el de contacto
  no pueden quedar desincronizados. Esta entidad guarda unicamente lo
  propio del ejercicio profesional.
 
  Campos respecto de la version inicial, con la HU que los exige:
    usuario            HU-06 "con su cuenta de usuario asociada"
    tarjetaProfesional HU-06 unicidad de la licencia
    tarifa             HU-08 y HU-10
   horaInicio/horaFin HU-07 jornada de atencion
 
  El estado no se duplica: se consulta sobre el usuario asociado, de modo
  que desactivar al veterinario (HU-09) y retirarle el acceso son la
  misma operacion.
 */
@Entity
@Table(
        name = "veterinarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_veterinarios_tarjeta", columnNames = "tarjeta_profesional"),
                @UniqueConstraint(name = "uk_veterinarios_usuario", columnNames = "id_usuario")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Veterinario {

    /** Jornada por defecto cuando no se indica otra (HU-07). */
    public static final LocalTime INICIO_POR_DEFECTO = LocalTime.of(8, 0);
    public static final LocalTime FIN_POR_DEFECTO = LocalTime.of(17, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_veterinario")
    private Long idVeterinario;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tarjeta_profesional", nullable = false, length = 50)
    private String tarjetaProfesional;

    @Column(nullable = false, length = 80)
    private String especialidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @PrePersist
    protected void alCrear() {
        if (horaInicio == null) {
            horaInicio = INICIO_POR_DEFECTO;
        }
        if (horaFin == null) {
            horaFin = FIN_POR_DEFECTO;
        }
    }

    /** Atajo para las vistas y para las reglas de HU-09 y HU-25. */
    public boolean estaActivo() {
        return usuario != null && usuario.getEstado() == EstadoUsuario.ACTIVO;
    }

    public String getNombre() {
        return usuario == null ? null : usuario.getNombre();
    }

    public String getCorreo() {
        return usuario == null ? null : usuario.getCorreo();
    }

    public String getDocumento() {
        return usuario == null ? null : usuario.getDocumento();
    }

    public String getTelefono() {
        return usuario == null ? null : usuario.getTelefono();
    }
}
