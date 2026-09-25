package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.DetalleRecetaCita;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
import com.dbodor.veterinariaweb.service.CitaService;

import java.time.LocalTime;
import java.util.List;

public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ProductoRepository productoRepository;

    public CitaServiceImpl(CitaRepository citaRepository, ProductoRepository productoRepository) {
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public DetalleRecetaCita finalizarCita(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cita con ID: " + idCita));

        DetalleRecetaCita detallePrescripcion = null;

        if (cita.getServicio() != null && "Consulta veterinaria".equalsIgnoreCase(cita.getServicio().getNombre())) {
            Producto antiparasitario = productoRepository.findByNombreContainingIgnoreCase("Antiparasitario")
                    .orElseThrow(() -> new IllegalStateException("No se encontró un producto antiparasitario en el catálogo"));

            if (antiparasitario.getStock() == null || antiparasitario.getStock() <= 0) {
                throw new IllegalStateException("El producto prescrito no cuenta con stock disponible");
            }

            detallePrescripcion = new DetalleRecetaCita();
            detallePrescripcion.setProducto(antiparasitario);
            detallePrescripcion.setCantidad(1);
            detallePrescripcion.setPosologiaIndicaciones("Prescripción sugerida automáticamente tras consulta médica");
            detallePrescripcion.setEsSugerenciaCompra(true);
        }

        cita.setEstado("FINALIZADA");
        citaRepository.save(cita);

        return detallePrescripcion;
    }

    @Override
    public Cita agendarCita(Cita cita) {
        if (cita.getServicio() == null || cita.getServicio().getDuracionMinutos() == null) {
            throw new IllegalArgumentException("La cita debe tener un servicio con duración válida");
        }

        LocalTime inicioNueva = cita.getHoraCita();
        LocalTime finNueva = inicioNueva.plusMinutes(cita.getServicio().getDuracionMinutos());

        List<Cita> citasExistentes = citaRepository.findByVeterinarioAndFechaCitaAndEstadoNot(
                cita.getVeterinario(),
                cita.getFechaCita(),
                "CANCELADA"
        );

        for (Cita existente : citasExistentes) {
            LocalTime inicioExistente = existente.getHoraCita();
            LocalTime finExistente = inicioExistente.plusMinutes(existente.getServicio().getDuracionMinutos());

            if (inicioNueva.isBefore(finExistente) && finNueva.isAfter(inicioExistente)) {
                throw new IllegalStateException("El veterinario ya tiene una cita asignada en ese rango horario");
            }
        }

        return citaRepository.save(cita);
    }

}
