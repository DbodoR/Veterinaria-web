package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.enums.EstadoProducto;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
import com.dbodor.veterinariaweb.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar(String texto) {
        return productoRepository.buscar(texto == null ? "" : texto.trim());
    }

    @Override
    public Producto registrarProducto(Producto producto) {
        if (producto.getPrecioUnitario() == null || producto.getPrecioUnitario() <= 0
                || producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El precio debe ser superior a cero y el stock no puede ser negativo");
        }

        producto.setEstado(EstadoProducto.ACTIVO);
        return productoRepository.save(producto);
    }

    @Override
    public Producto desactivarProducto(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto con ID: " + idProducto));

        producto.setEstado(EstadoProducto.INACTIVO);
        return productoRepository.save(producto);
    }
}