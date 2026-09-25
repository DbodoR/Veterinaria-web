package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.VeterinarioForm;
import com.dbodor.veterinariaweb.model.Veterinario;

import java.util.List;

/**
 * Operaciones del maestro de veterinarios (HU-06).
 */
public interface VeterinarioService {

    /** Resultado del alta: la ficha guardada y la clave de acceso. */
    record AltaVeterinario(Veterinario veterinario, String passwordTemporal) {
    }

    /** Listado con busqueda por nombre o tarjeta, y filtro por especialidad. */
    List<Veterinario> listar(String busqueda, String especialidad);

    /** Especialidades presentes, para alimentar el filtro del listado. */
    List<String> especialidades();

    /** Alta de un veterinario junto con su cuenta de acceso. */
    AltaVeterinario registrar(VeterinarioForm form);
}