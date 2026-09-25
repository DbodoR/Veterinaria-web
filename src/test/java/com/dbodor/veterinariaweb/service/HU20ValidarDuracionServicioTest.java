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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HU20ValidarDuracionServicioTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    @DisplayName("Criterio 1: Debe rechazar el servicio si la duración no es múltiplo de 15 minutos")
    void guardarServicio_duracionNoMultiploDe15_lanzaExcepcion() {
        Servicio servicioInvalido = new Servicio();
        servicioInvalido.setNombre("Corte de uñas express");
        servicioInvalido.setDescripcion("Corte rápido de uñas");
        servicioInvalido.setDuracionMinutos(25); // No es múltiplo de 15
        servicioInvalido.setPrecioBase(20000.0);
        servicioInvalido.setEstado(EstadoServicio.ACTIVO);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> servicioService.guardarServicio(servicioInvalido),
                "Debe lanzar IllegalArgumentException cuando la duración no es múltiplo de 15"
        );
        assertTrue(
                "La duración debe ser un múltiplo de 15 minutos".equals(excepcion.getMessage()),
                "El mensaje debe indicar que la duración debe ser múltiplo de 15 minutos"
        );
    }

    @Test
    @DisplayName("Criterio 2: Debe rechazar el servicio si la duración es menor a 15 minutos y explicar el rango")
    void guardarServicio_duracionMenorA15_lanzaExcepcionConMensajeExplicativo() {
        Servicio servicioMenor = new Servicio();
        servicioMenor.setNombre("Revisión");
        servicioMenor.setDescripcion("Revisión");
        servicioMenor.setDuracionMinutos(0);
        servicioMenor.setPrecioBase(40000.0);
        servicioMenor.setEstado(EstadoServicio.ACTIVO);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> servicioService.guardarServicio(servicioMenor),
                "Debe lanzar excepción si la duración es menor a 15 minutos"
        );

        assertTrue(
                "La duración debe estar entre 15 y 180 minutos".equals(excepcion.getMessage()),
                "El mensaje debe explicar el rango permitido de 15 a 180 minutos"
        );
    }

    @Test
    @DisplayName("Criterio 2: Debe rechazar el servicio si la duración es mayor a 180 minutos y explicar el rango")
    void guardarServicio_duracionMayorA180_lanzaExcepcionConMensajeExplicativo() {
        Servicio servicioMayor = new Servicio();
        servicioMayor.setNombre("Cirugía compleja reconstructiva");
        servicioMayor.setDescripcion("Procedimiento quirúrgico de alta complejidad");
        servicioMayor.setDuracionMinutos(195);
        servicioMayor.setPrecioBase(250000.0);
        servicioMayor.setEstado(EstadoServicio.ACTIVO);

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> servicioService.guardarServicio(servicioMayor),
                "Debe lanzar excepción si la duración supera los 180 minutos"
        );

        assertTrue(
                "La duración debe estar entre 15 y 180 minutos".equals(excepcion.getMessage()),
                "El mensaje debe explicar el rango permitido de 15 a 180 minutos"
        );
    }

    @Test
    @DisplayName("Criterio 3: Servicio de 60 min a las 09:00 bloquea la franja hasta las 10:00 impidiendo solapamientos")
    void agendarCita_servicio60Minutos_bloqueaFranjaHastaLas10() {
        Veterinario vet = new Veterinario();
        vet.setIdVeterinario(1L);

        Servicio servicioExistente = new Servicio();
        servicioExistente.setIdServicio(1L);
        servicioExistente.setNombre("Consulta general");
        servicioExistente.setDuracionMinutos(60); // 60 minutos de duración

        LocalDate fecha = LocalDate.of(2026, 10, 15);

        // Cita existente de 09:00 a 10:00
        Cita citaExistente = new Cita();
        citaExistente.setIdCita(100L);
        citaExistente.setVeterinario(vet);
        citaExistente.setServicio(servicioExistente);
        citaExistente.setFechaCita(fecha);
        citaExistente.setHoraCita(LocalTime.of(9, 0)); // 09:00
        citaExistente.setEstado("PROGRAMADA");

        // Intento de nueva cita a las 09:30 (dentro de la franja ocupada 09:00 a 10:00)
        Servicio servicioNuevo = new Servicio();
        servicioNuevo.setIdServicio(2L);
        servicioNuevo.setNombre("Vacunación");
        servicioNuevo.setDuracionMinutos(30);

        Cita nuevaCita = new Cita();
        nuevaCita.setVeterinario(vet);
        nuevaCita.setServicio(servicioNuevo);
        nuevaCita.setFechaCita(fecha);
        nuevaCita.setHoraCita(LocalTime.of(9, 30)); // 09:30

        when(citaRepository.findByVeterinarioAndFechaCitaAndEstadoNot(vet, fecha, "CANCELADA"))
                .thenReturn(List.of(citaExistente));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> citaService.agendarCita(nuevaCita),
                "Debe lanzar excepción porque la franja está ocupada hasta las 10:00"
        );

        assertTrue(
                "El veterinario ya tiene una cita asignada en ese rango horario".equals(excepcion.getMessage()),
                "El mensaje debe indicar conflicto por franja horaria ocupada"
        );
    }

}
