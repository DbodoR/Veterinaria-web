package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

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

        /**
     * Listado de la pantalla de reservas (HU-25). Trae mascota, dueno,
     * servicio y veterinario en la misma consulta, porque open-in-view esta
     * desactivado y la plantilla no puede cargar relaciones perezosas.
     */
    @Query("""
            select c from Cita c
              join fetch c.mascota m
              join fetch m.usuario
              join fetch c.servicio
              join fetch c.veterinario
            order by c.fechaCita desc, c.horaCita desc
            """)
    List<Cita> listarConDetalle();
}