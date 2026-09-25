package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.ClienteForm;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.service.ClienteService;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.GeneradorPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio del maestro de clientes (HU-01).
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneradorPassword generadorPassword;

    public ClienteServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              GeneradorPassword generadorPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.generadorPassword = generadorPassword;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar(String busqueda) {
        String texto = busqueda == null ? "" : busqueda.trim();
        return usuarioRepository.buscarPorRol(RolUsuario.CLIENTE, texto);
    }

    /**
     * Registra un cliente nuevo.
     *
     * El estado activo y la fecha de registro los pone la propia entidad en
     * su metodo @PrePersist, de modo que no hace falta asignarlos aqui.
     */
    @Override
    @Transactional
    public AltaCliente registrar(ClienteForm form) {
        if (usuarioRepository.existsByCorreoIgnoreCase(form.getCorreo())) {
            throw DatoDuplicadoException.correo();
        }
        if (usuarioRepository.existsByDocumento(form.getDocumento())) {
            throw DatoDuplicadoException.documento();
        }

        String temporal = generadorPassword.temporal();

        Usuario cliente = new Usuario();
        cliente.setNombre(form.getNombre().trim());
        cliente.setDocumento(form.getDocumento().trim());
        cliente.setCorreo(form.getCorreo().trim().toLowerCase());
        cliente.setTelefono(form.getTelefono().trim());
        cliente.setDireccion(form.getDireccion().trim());
        cliente.setCiudad(form.getCiudad().trim());
        cliente.setRol(RolUsuario.CLIENTE);
        cliente.setEstado(EstadoUsuario.ACTIVO);
        cliente.setContrasena(passwordEncoder.encode(temporal));
        cliente.setDebeCambiarPassword(Boolean.TRUE);

        return new AltaCliente(usuarioRepository.save(cliente), temporal);
    }
}