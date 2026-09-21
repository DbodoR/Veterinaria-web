package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    private String nombre;
    private String presentacion;

    @Column(name = "precio_unitario")
    private Double precioUnitario;
    private Integer stock;

    @Column(name = "es_recetable_veterinario")
    private Boolean esRecetableVeterinario;
    private String estado;

}
