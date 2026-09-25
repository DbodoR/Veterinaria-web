package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.ServicioForm;
import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.ServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Implementacion del catalogo de servicios.
 *
 * La anotacion @Service es imprescindible: sin ella Spring no registra la
 * clase y no puede inyectarse en los controladores.
 */
@Service
public class ServicioServiceImpl implements ServicioService {

    private static final Comparator<Servicio> POR_NOMBRE = Comparator.comparing(
            Servicio::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    /**
     * HU-19: el cliente solo ve servicios activos. Se ordena en memoria
     * para mantener la consulta simple y el catalogo en orden alfabetico.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarServiciosParaCliente() {
        return servicioRepository.findByEstado(EstadoServicio.ACTIVO)
                .stream()
                .sorted(POR_NOMBRE)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarTodosLosServicios() {
        return servicioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> buscar(String texto) {
        return servicioRepository.buscarPorTexto(texto == null ? "" : texto.trim());
    }

    /**
     * HU-20: la duracion debe caer en bloques de quince minutos, dentro del
     * rango permitido. Se valida aqui, y no solo en el formulario, para que
     * la regla se cumpla tambien cuando el servicio se guarde desde otro
     * punto del sistema.
     */
    @Override
    @Transactional
    public Servicio guardarServicio(Servicio servicio) {
        Integer duracion = servicio.getDuracionMinutos();
        if (duracion == null) {
            throw new IllegalArgumentException("La duración no puede ser nula");
        }
        if (duracion < Servicio.DURACION_MINIMA || duracion > Servicio.DURACION_MAXIMA) {
            throw new IllegalArgumentException("La duración debe estar entre 15 y 180 minutos");
        }
        if (duracion % Servicio.BLOQUE_MINUTOS != 0) {
            throw new IllegalArgumentException("La duración debe ser un múltiplo de 15 minutos");
        }
        return servicioRepository.save(servicio);
    }

    /**
     * HU-16: alta desde el maestro. Delega en guardarServicio para que las
     * validaciones de duracion se apliquen tambien por esta via.
     */
    @Override
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

        return guardarServicio(servicio);
    }
}