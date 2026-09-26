package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a mascotas. Lo usan el maestro de mascotas (HU-11, HU-12) y la
 * pantalla de agendamiento (HU-25).
 *
 * El estado de la mascota es texto; por convencion del equipo se usan
 * los valores "ACTIVO" e "INACTIVO", igual que en servicios.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    /**
     * Listado del maestro (HU-12). Filtra por texto libre sobre el nombre
     * de la mascota, el nombre del dueno o su documento. Un texto vacio
     * devuelve todas. Trae el dueno en la misma consulta para que la
     * plantilla pueda mostrarlo sin consultas adicionales.
     */
    @Query("""
            select m from Mascota m
              join fetch m.usuario u
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
               or lower(u.nombre) like lower(concat('%', :texto, '%'))
               or u.documento like concat('%', :texto, '%')
            order by m.nombre asc
            """)
    List<Mascota> buscar(@Param("texto") String texto);

    /** Mascotas de un cliente en un estado dado, para el agendamiento (HU-25). */
    @Query("""
            select m from Mascota m
            where m.usuario.idUsuario = :idUsuario
              and m.estado = :estado
            order by m.nombre asc
            """)
    List<Mascota> porDuenoYEstado(@Param("idUsuario") Long idUsuario,
                                  @Param("estado") String estado);

    /** Evita registrar dos mascotas con el mismo nombre para un mismo dueno (HU-11). */
    boolean existsByUsuarioIdUsuarioAndNombreIgnoreCase(Long idUsuario, String nombre);

        /** HU-24: la mascota con su dueno ya cargado, para la historia clinica. */
    @Query("select m from Mascota m join fetch m.usuario where m.idMascota = :id")
    Optional<Mascota> conDueno(@Param("id") Long id);
}