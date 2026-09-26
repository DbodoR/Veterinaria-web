package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombreContainingIgnoreCase(String nombre);

    /** Listado del maestro, filtrado por nombre y en orden alfabetico. */
    @Query("""
            select p from Producto p
            where lower(p.nombre) like lower(concat('%', :texto, '%'))
            order by p.nombre asc
            """)
    List<Producto> buscar(@Param("texto") String texto);
}