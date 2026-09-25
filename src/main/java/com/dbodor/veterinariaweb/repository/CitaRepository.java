package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Resumen del panel de administracion (HU-21). */
    long countByFechaCita(LocalDate fecha);

    long countByEstado(String estado);

    List<Cita> findByVeterinarioAndFechaCitaAndEstadoNot(Veterinario veterinario, LocalDate fechaCita, String estado);

    boolean existsByServicioAndEstado(Servicio servicio, String estado);

    List<Cita> findByEstado(String estado);
}