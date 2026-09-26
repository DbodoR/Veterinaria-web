package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.ReporteCitas;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Veterinario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.VeterinarioRepository;
import com.dbodor.veterinariaweb.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ReporteServiceImpl(CitaRepository citaRepository,
                              VeterinarioRepository veterinarioRepository) {
        this.citaRepository = citaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteCitas citasEIngresos(LocalDate desde, LocalDate hasta, Long idVeterinario) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }

        // CA4: la consulta no filtra por estado del veterinario ni del
        // servicio, asi que las citas de los desactivados siguen apareciendo.
        List<Cita> citas = citaRepository.reportePorRango(desde, hasta).stream()
                .filter(c -> idVeterinario == null
                        || idVeterinario.equals(c.getVeterinario().getIdVeterinario()))
                .toList();

        long finalizadas = citas.stream().filter(c -> ESTADO_FINALIZADA.equals(c.getEstado())).count();
        long canceladas = citas.stream().filter(c -> ESTADO_CANCELADA.equals(c.getEstado())).count();
        double ingresos = citas.stream()
                .filter(c -> ESTADO_FINALIZADA.equals(c.getEstado()))
                .mapToDouble(c -> c.getCostoTotal() == null ? 0.0 : c.getCostoTotal())
                .sum();

        return new ReporteCitas(citas, citas.size(), finalizadas, canceladas, ingresos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> todosLosVeterinarios() {
        return veterinarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Veterinario::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}