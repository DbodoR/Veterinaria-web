package com.dbodor.veterinariaweb.repository;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El correo es el identificador de acceso (HU-02). */
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    /** Unicidad de correo y documento, exigida por HU-01 y HU-03. */
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDocumento(String documento);

    /** Igual que el anterior pero ignorando al propio usuario que se edita (HU-03). */
    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, Long idUsuario);

    /** Resumen del panel (HU-21). */
    long countByRolAndEstado(RolUsuario rol, EstadoUsuario estado);
}