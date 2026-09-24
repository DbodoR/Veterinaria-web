package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.enums.EstadoProducto;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
import com.dbodor.veterinariaweb.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoEntrada;

    @BeforeEach
    public void setUp(){
        productoEntrada = new Producto();
        productoEntrada.setNombre("NexGard Spectra");
        productoEntrada.setPresentacion("Tableta masticable");
        productoEntrada.setPrecioUnitario(72000.00);
        productoEntrada.setStock(4);
        productoEntrada.setEsRecetableVeterinario(true);
    }

    @Test
    @DisplayName("Criterio 1: Registrar producto con estado ACTIVO, el sistema le asigna un ID único y lo devuelve")
    void registrarProducto(){
        Producto productoCreado = new Producto();
        productoCreado.setIdProducto(1L);
        productoCreado.setNombre("NexGard Spectra");
        productoCreado.setPresentacion("Tableta masticable");
        productoCreado.setPrecioUnitario(72000.00);
        productoCreado.setStock(4);
        productoCreado.setEsRecetableVeterinario(true);
        productoCreado.setEstado(EstadoProducto.ACTIVO);

        when(productoRepository.save(any(Producto.class))).thenReturn(productoCreado);

        Producto resultado = productoService.registrarProducto(productoEntrada);

        assertNotNull(resultado, "El producto resultante no debe ser nulo");
        assertNotNull(resultado.getIdProducto(), "El producto resultante debe tener un ID asignado");
        assertEquals(EstadoProducto.ACTIVO, resultado.getEstado(), "El estado inicial debe ser ACTIVO");
        assertEquals(productoEntrada.getNombre(), resultado.getNombre(), "El nombre del producto debe coincidir");
    }

    @Test
    @DisplayName("Criterio 2: Debe lanzar IllegalArgumentException cuando el precio es <= 0")
    void registrarProducto_precioInvalido() {
        productoEntrada = new Producto();
        productoEntrada.setPrecioUnitario(0.0);
        productoEntrada.setStock(10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.registrarProducto(productoEntrada)
        );

        assertEquals(
                "El precio debe ser superior a cero y el stock no puede ser negativo",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Criterio 2: Debe lanzar IllegalArgumentException si el stock es < 0")
    void registrarProducto_stockNegativo() {
        productoEntrada = new Producto();
        productoEntrada.setPrecioUnitario(45000.0);
        productoEntrada.setStock(-1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.registrarProducto(productoEntrada)
        );

        assertEquals(
                "El precio debe ser superior a cero y el stock no puede ser negativo",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Criterio 3: Debe cambiar el estado del producto a INACTIVO al desactivarlo")
    void desactivarProducto_cambiaEstadoAInactivo() {

        Long idProducto = 1L;
        Producto productoExistente = new Producto();
        productoExistente.setIdProducto(idProducto);
        productoExistente.setNombre("NexGard Spectra");
        productoExistente.setEstado(EstadoProducto.ACTIVO);

        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = productoService.desactivarProducto(idProducto);

        assertNotNull(resultado, "El producto resultante no debe ser nulo");
        assertEquals(EstadoProducto.INACTIVO, resultado.getEstado(), "El estado del producto debe ser INACTIVO");
    }

}
