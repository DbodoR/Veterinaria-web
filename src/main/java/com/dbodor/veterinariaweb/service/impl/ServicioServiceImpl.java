package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.service.ServicioService;

import java.util.List;

public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    private final CitaRepository citaRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository, CitaRepository citaRepository){
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
    }
    @Override
    public List<Servicio> listarServiciosParaCliente() {
        return servicioRepository.findByEstado(EstadoServicio.ACTIVO);
    }

    @Override
    public List<Servicio> listarTodosLosServicios() {
        return servicioRepository.findAll();
    }

    @Override
    public Servicio guardarServicio(Servicio servicio) {
        if (servicio.getDuracionMinutos() == null) {
            throw new IllegalArgumentException("La duración no puede ser nula");
        }

        if (servicio.getDuracionMinutos() < 15 || servicio.getDuracionMinutos() > 180) {
            throw new IllegalArgumentException("La duración debe estar entre 15 y 180 minutos");
        }

        if (servicio.getDuracionMinutos() % 15 != 0) {
            throw new IllegalArgumentException("La duración debe ser un múltiplo de 15 minutos");
        }

        return servicioRepository.save(servicio);
    }

    @Override
    public Servicio actualizarPrecio(Long idServicio, Double nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio <= 0.0) {
            throw new IllegalArgumentException("El precio debe ser un valor positivo");
        }

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + idServicio));

        servicio.setPrecioBase(nuevoPrecio);
        return servicioRepository.save(servicio);
    }

    @Override
    public Servicio desactivarServicio(Long idServicio) {
        return desactivarServicio(idServicio, false);
    }

    @Override
    public Servicio desactivarServicio(Long idServicio, boolean confirmacion) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + idServicio));

        boolean tieneCitasPendientes = citaRepository.existsByServicioAndEstado(servicio, "PROGRAMADA");

        if (tieneCitasPendientes && !confirmacion) {
            throw new IllegalStateException(
                    "El servicio tiene citas pendientes asociadas. Se requiere confirmación explícita para desactivarlo."
            );
        }

        servicio.setEstado(EstadoServicio.INACTIVO);
        return servicioRepository.save(servicio);
    }

}
