package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.MascotaForm;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Usuario;

import java.util.List;

/**
 * Maestro de mascotas.
 *   HU-11  alta con dueno obligatorio y nombre unico por dueno
 *   HU-12  listado con busqueda por mascota, dueno o documento
 */
public interface MascotaService {

    String ESTADO_ACTIVO = "ACTIVO";

    /** HU-12: listado; un texto vacio o nulo trae todas. */
    List<Mascota> buscar(String texto);

    /** HU-11 CA2: clientes que pueden ser duenos. */
    List<Usuario> clientesActivos();

    /**
     * HU-11: registra la mascota activa y asociada a su dueno.
     * Lanza IllegalArgumentException si el dueno no es un cliente activo y
     * DatoDuplicadoException si el dueno ya tiene una mascota con ese nombre.
     */
    Mascota registrar(MascotaForm form);
}