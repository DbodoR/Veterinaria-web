package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.Producto;

public interface ProductoService {
    Producto registrarProducto(Producto producto);
    Producto desactivarProducto(Long idProducto);
}
