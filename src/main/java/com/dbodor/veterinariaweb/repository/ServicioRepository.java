package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /** Unicidad del nombre (HU-16 criterio 2). */
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdServicioNot(String nombre, Long idServicio);

    Optional<Servicio> findByNombreIgnoreCase(String nombre);

    /** Catalogo visible para el cliente y para la pantalla de reservas (HU-19, HU-25). */
    List<Servicio> findByEstadoOrderByNombreAsc(EstadoServicio estado);

    /** Listado del maestro, con busqueda libre por nombre o descripcion. */
    @Query("""
            select s from Servicio s
            where lower(s.nombre) like lower(concat('%', :texto, '%'))
               or lower(coalesce(s.descripcion, '')) like lower(concat('%', :texto, '%'))
            order by s.nombre asc
            """)
    List<Servicio> buscar(@Param("texto") String texto);
}