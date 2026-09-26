package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.Producto;

import java.util.List;

public interface ProductoService {

    /** Listado del maestro con busqueda por nombre; vacio o nulo trae todos. */
    List<Producto> listar(String texto);

    Producto registrarProducto(Producto producto);

    Producto desactivarProducto(Long idProducto);
}