package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.CitaForm;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.model.Veterinario;

import java.util.List;

/**
 * Pantalla transaccional de agendamiento (HU-25).
 *
 * Reune lo que necesita el formulario (clientes, mascotas, servicios y
 * veterinarios disponibles) y la operacion de agendar, que valida las
 * reglas del negocio antes de guardar.
 */
public interface AgendaCitaService {

    String ESTADO_PROGRAMADA = "PROGRAMADA";
    String MASCOTA_ACTIVA = "ACTIVO";

    List<Usuario> clientesActivos();

    /** CA2: solo las mascotas activas del cliente elegido. */
    List<Mascota> mascotasActivas(Long idCliente);

    /** CA1: solo los servicios en estado activo. */
    List<Servicio> serviciosActivos();

    /** CA1: solo los veterinarios cuya cuenta esta activa. */
    List<Veterinario> veterinariosActivos();

    /** CA7: listado de citas con sus datos relacionados ya cargados. */
    List<Cita> listar();

    /** Dueno de una mascota, para volver a cargar el formulario tras un error. */
    Long duenoDeMascota(Long idMascota);

    /**
     * Agenda la cita. Lanza IllegalArgumentException o IllegalStateException
     * con un mensaje para el usuario si alguna regla no se cumple.
     */
    Cita agendar(CitaForm form);
}