package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo"),
                @UniqueConstraint(name = "uk_usuarios_documento", columnNames = "documento")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String documento;

    @Column(nullable = false, length = 150)
    private String correo;

    /** Hash BCrypt, nunca la contrasena en texto plano. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(length = 30)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = Boolean.FALSE;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Se ejecuta antes del primer INSERT. Deja la fecha de registro
     * y los valores por defecto sin que el servicio tenga que recordarlo.
     */
    @PrePersist
    protected void alCrear() {
        fechaRegistro = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoUsuario.ACTIVO;
        }
        if (debeCambiarPassword == null) {
            debeCambiarPassword = Boolean.FALSE;
        }
        if (intentosFallidos == null) {
            intentosFallidos = 0;
        }
    }

    /**
     * Se ejecuta antes de cada UPDATE. Con esto el segundo criterio de
     * HU-03 ("los cambios quedan registrados junto con la fecha de
     * modificacion") se cumple solo, sin codigo en el servicio.
     */
    @PreUpdate
    protected void alActualizar() {
        fechaModificacion = LocalDateTime.now();
    }
}