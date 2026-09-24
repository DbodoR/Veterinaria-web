package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.ClienteForm;
import com.dbodor.veterinariaweb.model.EstadoUsuario;
import com.dbodor.veterinariaweb.model.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * Reglas de negocio del maestro de clientes (HU-01).
 */
@Service
public class ClienteService {

    /** Caracteres de la contrasena temporal. Se omiten los ambiguos (O, 0, l, 1). */
    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO_TEMPORAL = 10;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public ClienteService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Resultado del alta: el cliente guardado y la clave que hay que entregarle. */
    public record AltaCliente(Usuario cliente, String passwordTemporal) {
    }

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
    @Transactional
    public AltaCliente registrar(ClienteForm form) {
        if (usuarioRepository.existsByCorreoIgnoreCase(form.getCorreo())) {
            throw DatoDuplicadoException.correo();
        }
        if (usuarioRepository.existsByDocumento(form.getDocumento())) {
            throw DatoDuplicadoException.documento();
        }

        String temporal = generarPasswordTemporal();

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

    /**
     * Clave de un solo uso que el administrador entrega al cliente.
     * En el primer ingreso el sistema le exigira cambiarla (HU-05).
     */
    private String generarPasswordTemporal() {
        StringBuilder sb = new StringBuilder(LARGO_TEMPORAL);
        for (int i = 0; i < LARGO_TEMPORAL; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}