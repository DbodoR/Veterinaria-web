package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.VeterinarioForm;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.model.Veterinario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.repository.VeterinarioRepository;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.GeneradorPassword;
import com.dbodor.veterinariaweb.service.VeterinarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * Reglas de negocio del maestro de veterinarios (HU-06).
 */
@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneradorPassword generadorPassword;

    public VeterinarioServiceImpl(VeterinarioRepository veterinarioRepository,
                                  UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder,
                                  GeneradorPassword generadorPassword) {
        this.veterinarioRepository = veterinarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.generadorPassword = generadorPassword;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> listar(String busqueda, String especialidad) {
        return veterinarioRepository.buscar(
                busqueda == null ? "" : busqueda.trim(),
                especialidad == null ? "" : especialidad.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> especialidades() {
        return veterinarioRepository.especialidades();
    }

    /**
     * Registra un veterinario y su cuenta de acceso en una sola transaccion.
     *
     * Si algo falla a mitad de camino, la anotacion @Transactional garantiza
     * que no quede un usuario huerfano sin ficha profesional.
     */
    @Override
    @Transactional
    public AltaVeterinario registrar(VeterinarioForm form) {
        if (usuarioRepository.existsByCorreoIgnoreCase(form.getCorreo())) {
            throw DatoDuplicadoException.correo();
        }
        if (usuarioRepository.existsByDocumento(form.getDocumento())) {
            throw DatoDuplicadoException.documento();
        }
        if (veterinarioRepository.existsByTarjetaProfesional(form.getTarjetaProfesional())) {
            throw DatoDuplicadoException.tarjetaProfesional();
        }

        String temporal = generadorPassword.temporal();

        Usuario cuenta = new Usuario();
        cuenta.setNombre(form.getNombre().trim());
        cuenta.setDocumento(form.getDocumento().trim());
        cuenta.setCorreo(form.getCorreo().trim().toLowerCase());
        cuenta.setTelefono(form.getTelefono().trim());
        cuenta.setRol(RolUsuario.VETERINARIO);
        cuenta.setEstado(EstadoUsuario.ACTIVO);
        cuenta.setContrasena(passwordEncoder.encode(temporal));
        cuenta.setDebeCambiarPassword(Boolean.TRUE);
        usuarioRepository.save(cuenta);

        Veterinario veterinario = new Veterinario();
        veterinario.setUsuario(cuenta);
        veterinario.setTarjetaProfesional(form.getTarjetaProfesional().trim());
        veterinario.setEspecialidad(form.getEspecialidad().trim());
        veterinario.setTarifa(form.getTarifa());
        veterinario.setHoraInicio(form.getHoraInicio());
        veterinario.setHoraFin(form.getHoraFin());

        return new AltaVeterinario(veterinarioRepository.save(veterinario), temporal);
    }
}