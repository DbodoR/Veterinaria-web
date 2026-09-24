package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.service.impl.ServicioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HU19ConsultarServiciosTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    @Test
    @DisplayName("Criterio 1: Cliente solo debe visualizar servicios en estado activo")
    void listarServiciosParaCliente_retornaSoloServiciosActivos() {

        Servicio s1 = new Servicio();
        s1.setIdServicio(1L);
        s1.setNombre("Consulta general");
        s1.setEstado(EstadoServicio.ACTIVO);

        Servicio s2 = new Servicio();
        s2.setIdServicio(2L);
        s2.setNombre("Vacunación antirrábica");
        s2.setEstado(EstadoServicio.ACTIVO);

        when(servicioRepository.findByEstado(EstadoServicio.ACTIVO)).thenReturn(List.of(s1, s2));

        List<Servicio> resultado = servicioService.listarServiciosParaCliente();

        assertNotNull(resultado, "La lista de servicios no debe ser nula");
        assertEquals(2, resultado.size(), "Debe retornar únicamente los 2 servicios activos");
        assertTrue(resultado.stream().allMatch(s -> s.getEstado() == EstadoServicio.ACTIVO), "Todos los servicios listados deben estar activos");
    }

    @Test
    @DisplayName("Criterio 2: Cada servicio del catálogo debe contener nombre, descripción, duración y precio")
    void listarServiciosParaCliente_validaCamposObligatorios() {
        Servicio servicioCompleto = new Servicio();
        servicioCompleto.setIdServicio(1L);
        servicioCompleto.setNombre("Consulta general");
        servicioCompleto.setDescripcion("Evaluación física completa del paciente");
        servicioCompleto.setDuracionMinutos(30);
        servicioCompleto.setPrecioBase(45000.00);
        servicioCompleto.setEstado(EstadoServicio.ACTIVO);

        when(servicioRepository.findByEstado(EstadoServicio.ACTIVO))
                .thenReturn(List.of(servicioCompleto));

        List<Servicio> resultado = servicioService.listarServiciosParaCliente();

        assertNotNull(resultado, "La lista no debe ser nula");
        assertFalse(resultado.isEmpty(), "La lista debe contener elementos");

        Servicio servicio = resultado.getFirst();
        assertNotNull(servicio.getNombre(), "El nombre del servicio no debe ser nulo");
        assertFalse(servicio.getNombre().isBlank(), "El nombre del servicio no debe estar vacío");

        assertNotNull(servicio.getDescripcion(), "La descripción no debe ser nula");
        assertFalse(servicio.getDescripcion().isBlank(), "La descripción no debe estar vacía");

        assertNotNull(servicio.getDuracionMinutos(), "La duración no debe ser nula");
        assertTrue(servicio.getDuracionMinutos() > 0, "La duración debe ser mayor a cero minutos");

        assertNotNull(servicio.getPrecioBase(), "El precio no debe ser nulo");
        assertTrue(servicio.getPrecioBase() > 0, "El precio debe ser mayor a cero");
    }

    @Test
    @DisplayName("Criterio 3: Administrador debe visualizar tanto servicios activos como inactivos en el maestro")
    void listarTodosLosServicios_retornaActivosEInactivosParaAdmin() {
        Servicio sActivo = new Servicio();
        sActivo.setIdServicio(1L);
        sActivo.setNombre("Consulta general");
        sActivo.setEstado(EstadoServicio.ACTIVO);

        Servicio sInactivo = new Servicio();
        sInactivo.setIdServicio(2L);
        sInactivo.setNombre("Peluquería canina avanzada");
        sInactivo.setEstado(EstadoServicio.INACTIVO);

        when(servicioRepository.findAll()).thenReturn(List.of(sActivo, sInactivo));

        List<Servicio> resultado = servicioService.listarTodosLosServicios();

        assertNotNull(resultado, "La lista de servicios para admin no debe ser nula");
        assertTrue(resultado.size() == 2, "Debe contener el total de servicios sin filtrar");

        boolean contieneActivo = resultado.stream().anyMatch(s -> s.getEstado() == EstadoServicio.ACTIVO);
        boolean contieneInactivo = resultado.stream().anyMatch(s -> s.getEstado() == EstadoServicio.INACTIVO);

        assertTrue(contieneActivo, "El maestro debe incluir los servicios en estado ACTIVO");
        assertTrue(contieneInactivo, "El maestro debe incluir los servicios en estado INACTIVO marcados como tales");
    }

}
