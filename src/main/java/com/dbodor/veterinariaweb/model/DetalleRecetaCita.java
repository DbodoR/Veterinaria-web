package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_receta_cita")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetalleRecetaCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleReceta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historia", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    private Integer cantidad;

    @Column(name = "posologia_indicaciones")
    private String posologiaIndicaciones;

    @Column(name = "es_sugerencia_compra")
    private Boolean esSugerenciaCompra;

}
