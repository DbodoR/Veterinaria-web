package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Veterinario;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HU17ModificarPrecioServicioTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    @DisplayName("Criterio 1: Modificar el precio de un servicio aplica el nuevo valor a citas posteriores")
    void actualizarPrecio_aplicaSoloANuevasCitasAgendadas() {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(1L);
        servicio.setNombre("Consulta general");
        servicio.setDuracionMinutos(30);
        servicio.setPrecioBase(50000.0);
        servicio.setEstado(EstadoServicio.ACTIVO);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Servicio servicioActualizado = servicioService.actualizarPrecio(1L, 60000.0);

        assertNotNull(servicioActualizado, "El servicio actualizado no debe ser nulo");
        assertEquals(60000.0, servicioActualizado.getPrecioBase(), "El nuevo precio base debe ser 60000.0");

        Veterinario vet = new Veterinario();
        vet.setIdVeterinario(1L);

        Cita nuevaCita = new Cita();
        nuevaCita.setVeterinario(vet);
        nuevaCita.setServicio(servicioActualizado);
        nuevaCita.setFechaCita(LocalDate.of(2026, 11, 1));
        nuevaCita.setHoraCita(LocalTime.of(10, 0));

        when(citaRepository.findByVeterinarioAndFechaCitaAndEstadoNot(any(), any(), any()))
                .thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita citaAgendada = citaService.agendarCita(nuevaCita);

        assertNotNull(citaAgendada.getCostoTotal(), "El costo total de la nueva cita no debe ser nulo");
        assertEquals(60000.0, citaAgendada.getCostoTotal(),
                "La nueva cita debe registrarse con la tarifa vigente de 60000.0");
    }

    @Test
    @DisplayName("Criterio 2: Modificar el precio del servicio no altera el costoTotal de citas ya atendidas")
    void actualizarPrecio_citasAtendidasConservanPrecioHistorico() {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(1L);
        servicio.setNombre("Desparasitación");
        servicio.setDuracionMinutos(15);
        servicio.setPrecioBase(40000.0);
        servicio.setEstado(EstadoServicio.ACTIVO);

        Cita citaAtendida = new Cita();
        citaAtendida.setIdCita(50L);
        citaAtendida.setServicio(servicio);
        citaAtendida.setFechaCita(LocalDate.of(2026, 8, 10));
        citaAtendida.setHoraCita(LocalTime.of(14, 0));
        citaAtendida.setEstado("ATENDIDA");
        citaAtendida.setCostoTotal(40000.0);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Servicio servicioModificado = servicioService.actualizarPrecio(1L, 55000.0);

        assertEquals(55000.0, servicioModificado.getPrecioBase(), "El catálogo de servicios debe reflejar 55000.0");
        assertEquals(40000.0, citaAtendida.getCostoTotal(),
                "El costoTotal registrado en la cita atendida debe permanecer inalterado en 40000.0");
        assertNotEquals(servicioModificado.getPrecioBase(), citaAtendida.getCostoTotal(),
                "El costo histórico de la cita no debe igualarse al nuevo precio de catálogo");
    }

}
