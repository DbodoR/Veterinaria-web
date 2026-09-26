package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.dto.MascotaForm;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.MascotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository,
                              UsuarioRepository usuarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> buscar(String texto) {
        return mascotaRepository.buscar(texto == null ? "" : texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> clientesActivos() {
        return usuarioRepository.buscarPorRol(RolUsuario.CLIENTE, "").stream()
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .toList();
    }

    @Override
    @Transactional
    public Mascota registrar(MascotaForm form) {
        // CA2: el formulario solo ofrece clientes activos, pero el id puede
        // llegar manipulado, asi que se vuelve a comprobar aqui.
        Usuario dueno = usuarioRepository.findById(form.getIdCliente())
                .filter(u -> u.getRol() == RolUsuario.CLIENTE)
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .orElseThrow(() -> new IllegalArgumentException("El dueno debe ser un cliente activo"));

        String nombre = form.getNombre().trim();
        if (mascotaRepository.existsByUsuarioIdUsuarioAndNombreIgnoreCase(dueno.getIdUsuario(), nombre)) {
            throw new DatoDuplicadoException("nombre", "Este cliente ya tiene una mascota con ese nombre");
        }

        Mascota mascota = new Mascota();
        mascota.setUsuario(dueno);
        mascota.setNombre(nombre);
        mascota.setEspecie(form.getEspecie());
        mascota.setRaza(form.getRaza() == null || form.getRaza().isBlank() ? null : form.getRaza().trim());
        mascota.setSexo(form.getSexo());
        mascota.setFechaNacimiento(form.getFechaNacimiento());
        mascota.setPesoKg(form.getPesoKg());
        mascota.setEstado(ESTADO_ACTIVO);

        return mascotaRepository.save(mascota);
    }
}