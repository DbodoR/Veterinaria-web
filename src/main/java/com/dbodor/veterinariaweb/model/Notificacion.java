package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String titulo;
    private String mensaje;
    private String tipo;
    private Boolean leida;

    @Column(name = "enlace_redireccion")
    private String enlaceRedireccion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
