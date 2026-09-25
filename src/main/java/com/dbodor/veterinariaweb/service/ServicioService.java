package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.ServicioForm;
import com.dbodor.veterinariaweb.model.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio del maestro de servicios (HU-16).
 */
@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<Servicio> listar(String busqueda) {
        return servicioRepository.buscar(busqueda == null ? "" : busqueda.trim());
    }

    /** Catalogo para el cliente y para la pantalla de reservas (HU-19, HU-25). */
    @Transactional(readOnly = true)
    public List<Servicio> activos() {
        return servicioRepository.findByEstadoOrderByNombreAsc(EstadoServicio.ACTIVO);
    }

    @Transactional
    public Servicio crear(ServicioForm form) {
        if (servicioRepository.existsByNombreIgnoreCase(form.getNombre().trim())) {
            throw DatoDuplicadoException.nombreServicio();
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(form.getNombre().trim());
        servicio.setDescripcion(form.getDescripcion() == null ? null : form.getDescripcion().trim());
        servicio.setDuracionMinutos(form.getDuracionMinutos());
        servicio.setPrecioBase(form.getPrecioBase());
        servicio.setEsConsultaVeterinaria(Boolean.TRUE.equals(form.getEsConsultaVeterinaria()));
        servicio.setEstado(EstadoServicio.ACTIVO);

        return servicioRepository.save(servicio);
    }
}