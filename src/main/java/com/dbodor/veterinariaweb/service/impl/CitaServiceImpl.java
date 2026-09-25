package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.DetalleRecetaCita;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
import com.dbodor.veterinariaweb.service.CitaService;

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

}
