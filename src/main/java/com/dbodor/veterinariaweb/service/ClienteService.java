package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.ClienteForm;
import com.dbodor.veterinariaweb.dto.PerfilClienteDto;
import com.dbodor.veterinariaweb.model.Usuario;

import java.util.List;

/**
 * Operaciones del maestro de clientes (HU-01).
 */
public interface ClienteService {

    /** Resultado del alta: el cliente guardado y la clave que hay que entregarle. */
    record AltaCliente(Usuario cliente, String passwordTemporal) {
    }

    /** Listado con busqueda por nombre o documento. */
    List<Usuario> listar(String busqueda);

    /** Alta de un cliente nuevo con su contrasena temporal. */
    AltaCliente registrar(ClienteForm form);

    PerfilClienteDto consultarPerfil(Long idUsuario);

    PerfilClienteDto actualizarPerfil(Long idUsuario, PerfilClienteDto cambios);
}