package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    /** Unicidad de la licencia profesional (HU-06 CA2). */
    boolean existsByTarjetaProfesional(String tarjetaProfesional);

    Optional<Veterinario> findByTarjetaProfesional(String tarjetaProfesional);

    /** Especialidades presentes, para alimentar el filtro del listado. */
    @Query("select distinct v.especialidad from Veterinario v order by v.especialidad asc")
    List<String> especialidades();

    /**
     * Listado del maestro (HU-06). Filtra por texto libre sobre el nombre
     * del profesional o su tarjeta, y opcionalmente por especialidad.
     * Un texto o una especialidad vacios no acotan el resultado.
     */
    @Query("""
            select v from Veterinario v
              join v.usuario u
            where ( lower(u.nombre) like lower(concat('%', :texto, '%'))
                 or lower(v.tarjetaProfesional) like lower(concat('%', :texto, '%')) )
              and ( :especialidad = '' or v.especialidad = :especialidad )
            order by u.nombre asc
            """)
    List<Veterinario> buscar(@Param("texto") String texto,
                             @Param("especialidad") String especialidad);

    /** Profesionales activos, para la pantalla de reservas (HU-10, HU-25). */
    @Query("""
            select v from Veterinario v
              join v.usuario u
            where u.estado = :estado
            order by u.nombre asc
            """)
    List<Veterinario> porEstado(@Param("estado") EstadoUsuario estado);
}