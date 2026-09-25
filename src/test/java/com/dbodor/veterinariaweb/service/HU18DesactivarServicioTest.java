package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.service.impl.CitaServiceImpl;
import com.dbodor.veterinariaweb.service.impl.ServicioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HU18DesactivarServicioTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    @DisplayName("Criterio 1: Desactivar un servicio lo excluye de la pantalla de reservas (cliente)")
    void desactivarServicio_servicioActivo_dejaDeAparecerEnReservas() {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(1L);
        servicio.setNombre("Peluquería Canina");
        servicio.setDuracionMinutos(45);
        servicio.setPrecioBase(35000.0);
        servicio.setEstado(EstadoServicio.ACTIVO);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Servicio servicioDesactivado = servicioService.desactivarServicio(1L);

        assertNotNull(servicioDesactivado, "El servicio retornado no debe ser nulo");
        assertEquals(EstadoServicio.INACTIVO, servicioDesactivado.getEstado(),
                "El servicio debe quedar con estado INACTIVO");

        when(servicioRepository.findByEstado(EstadoServicio.ACTIVO)).thenReturn(List.of());

        List<Servicio> serviciosParaCliente = servicioService.listarServiciosParaCliente();

        assertTrue(serviciosParaCliente.isEmpty(),
                "La lista para cliente no debe incluir servicios inactivos");
        assertFalse(serviciosParaCliente.contains(servicioDesactivado),
                "El servicio desactivado no debe figurar en la pantalla de reservas");
    }

    @Test
    @DisplayName("Criterio 2: Desactivar servicio con citas pendientes sin confirmación lanza advertencia")
    void desactivarServicio_conCitasPendientesSinConfirmacion_lanzaAdvertencia() {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(1L);
        servicio.setNombre("Vacunación");
        servicio.setEstado(EstadoServicio.ACTIVO);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        // Simula que existen citas pendientes en estado PROGRAMADA
        when(citaRepository.existsByServicioAndEstado(servicio, "PROGRAMADA")).thenReturn(true);

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> servicioService.desactivarServicio(1L, false),
                "Debe advertir y requerir confirmación explícita si hay citas pendientes"
        );
        assertEquals(
                "El servicio tiene citas pendientes asociadas. Se requiere confirmación explícita para desactivarlo.",
                excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("Criterio 3: El reporte desglosado incluye ingresos de servicios inactivos en el histórico")
    void obtenerReporteIngresosPorServicio_incluyeServiciosInactivos() {
        Servicio servicioInactivo = new Servicio();
        servicioInactivo.setIdServicio(1L);
        servicioInactivo.setNombre("Ecografía Básica");
        servicioInactivo.setEstado(EstadoServicio.INACTIVO);

        Servicio servicioActivo = new Servicio();
        servicioActivo.setIdServicio(2L);
        servicioActivo.setNombre("Consulta General");
        servicioActivo.setEstado(EstadoServicio.ACTIVO);

        Cita cita1 = new Cita();
        cita1.setServicio(servicioInactivo);
        cita1.setEstado("ATENDIDA");
        cita1.setCostoTotal(80000.0);

        Cita cita2 = new Cita();
        cita2.setServicio(servicioActivo);
        cita2.setEstado("ATENDIDA");
        cita2.setCostoTotal(50000.0);

        when(citaRepository.findByEstado("ATENDIDA")).thenReturn(List.of(cita1, cita2));

        Map<String, Double> reporte = citaService.obtenerReporteIngresosPorServicio();

        assertNotNull(reporte);
        assertEquals(2, reporte.size(), "El reporte debe incluir tanto servicios activos como inactivos");
        assertEquals(80000.0, reporte.get("Ecografía Básica"), "El servicio inactivo debe registrar 80000.0");
        assertEquals(50000.0, reporte.get("Consulta General"), "El servicio activo debe registrar 50000.0");
    }

}
