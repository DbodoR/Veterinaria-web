package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /* El correo es el identificador de acceso (HU-02). */
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    /* Unicidad de correo y documento, exigida por HU-01 y HU-03. */
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDocumento(String documento);

    /* Igual que los anteriores pero ignorando al propio usuario que se edita (HU-03). */
    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, Long idUsuario);

    boolean existsByDocumentoAndIdUsuarioNot(String documento, Long idUsuario);

    /* Resumen del panel (HU-21). */
    long countByRolAndEstado(RolUsuario rol, EstadoUsuario estado);

    /*
      Listado del maestro con busqueda por nombre o documento (HU-01).
      Un texto vacio devuelve todos los usuarios del rol indicado.
     */
    @Query("""
            select u from Usuario u
            where u.rol = :rol
              and ( lower(u.nombre) like lower(concat('%', :texto, '%'))
                 or u.documento like concat('%', :texto, '%') )
            order by u.nombre asc
            """)
    List<Usuario> buscarPorRol(@Param("rol") RolUsuario rol, @Param("texto") String texto);
}