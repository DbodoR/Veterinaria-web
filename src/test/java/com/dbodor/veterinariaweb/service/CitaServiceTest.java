package com.dbodor.veterinariaweb.service;


import com.dbodor.veterinariaweb.enums.EstadoProducto;
import com.dbodor.veterinariaweb.model.*;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
import com.dbodor.veterinariaweb.service.impl.CitaServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    @DisplayName("Criterio 4: Al finalizar cita de 'Consulta veterinaria' debe prescribir antiparasitario predeterminado")
    void finalizarCita_prescribeAntiparasitarioPredeterminado() {
        Long idCita = 1L;

        Servicio servicioConsulta = new Servicio();
        servicioConsulta.setNombre("Consulta veterinaria");

        Cita cita = new Cita();
        cita.setIdCita(idCita);
        cita.setServicio(servicioConsulta);
        cita.setEstado("PROGRAMADA");

        Producto antiparasitario = new Producto();
        antiparasitario.setIdProducto(10L);
        antiparasitario.setNombre("Antiparasitario Canino/Felino");
        antiparasitario.setEstado(EstadoProducto.ACTIVO);
        antiparasitario.setStock(20);

        when(citaRepository.findById(idCita)).thenReturn(Optional.of(cita));
        when(productoRepository.findByNombreContainingIgnoreCase("Antiparasitario"))
                .thenReturn(Optional.of(antiparasitario));

        DetalleRecetaCita resultado = citaService.finalizarCita(idCita);

        assertNotNull(resultado, "El detalle de la receta prescrita no debe ser nulo");
        assertNotNull(resultado.getProducto(), "Debe contener el producto prescrito");
        assertEquals("El producto prescrito debe ser el antiparasitario predeterminado", "Antiparasitario Canino/Felino", resultado.getProducto().getNombre());
        Assertions.assertEquals(1, (int) resultado.getCantidad(), "La dosis sugerida por defecto debe ser 1");
        assertTrue(resultado.getEsSugerenciaCompra(), "Debe marcarse como sugerencia de compra / notificación");
        assertEquals("El estado de la cita debe actualizarse a FINALIZADA", "FINALIZADA", cita.getEstado());
    }

    @Test
    @DisplayName("Criterio 5: Debe lanzar IllegalStateException si el producto a prescribir no tiene stock disponible")
    void finalizarCita_antiparasitarioSinStock_lanzaExcepcion() {
        Long idCita = 2L;

        Servicio servicioConsulta = new Servicio();
        servicioConsulta.setNombre("Consulta veterinaria");

        Cita cita = new Cita();
        cita.setIdCita(idCita);
        cita.setServicio(servicioConsulta);
        cita.setEstado("PROGRAMADA");

        Producto antiparasitarioAgotado = new Producto();
        antiparasitarioAgotado.setIdProducto(10L);
        antiparasitarioAgotado.setNombre("Antiparasitario Canino/Felino");
        antiparasitarioAgotado.setEstado(EstadoProducto.ACTIVO);
        antiparasitarioAgotado.setStock(0);

        when(citaRepository.findById(idCita)).thenReturn(Optional.of(cita));
        when(productoRepository.findByNombreContainingIgnoreCase("Antiparasitario"))
                .thenReturn(Optional.of(antiparasitarioAgotado));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> citaService.finalizarCita(idCita),
                "Debe lanzar excepción si el producto a prescribir no cuenta con stock disponible"
        );

        assertTrue(
                "El producto prescrito no cuenta con stock disponible".equals(exception.getMessage()),
                "El mensaje de error debe alertar sobre la indisponibilidad de stock"
        );
    }

}
