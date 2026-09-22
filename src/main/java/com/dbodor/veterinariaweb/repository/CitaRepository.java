package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Resumen del panel de administracion (HU-21). */
    long countByFechaCita(LocalDate fecha);

    long countByEstado(String estado);
}