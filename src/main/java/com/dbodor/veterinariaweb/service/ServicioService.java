package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.dto.ServicioForm;
import com.dbodor.veterinariaweb.model.Servicio;

import java.util.List;

/**
 *
 * Reune lo que exigen tres historias:
 *   HU-19  consulta del catalogo por el cliente y por el administrador
 *   HU-20  validacion de la duracion en bloques de quince minutos
 *   HU-16  alta desde el maestro, con control de nombre duplicado
 */
public interface ServicioService {

    /** HU-19 y HU-25: solo los servicios en estado activo. */
    List<Servicio> listarServiciosParaCliente();

    /** HU-19: el administrador ve tambien los inactivos. */
    List<Servicio> listarTodosLosServicios();

    /** HU-16: listado del maestro con busqueda por nombre o descripcion. */
    List<Servicio> buscar(String texto);

    /** HU-20: guarda validando que la duracion caiga en bloques validos. */
    Servicio guardarServicio(Servicio servicio);

    /** HU-16: alta desde el formulario del maestro. */
    Servicio crear(ServicioForm form);

    /** HU-17: cambia el precio base de un servicio existente. */
    Servicio actualizarPrecio(Long idServicio, Double nuevoPrecio);

    /** HU-18: desactiva sin confirmacion; falla si hay citas pendientes. */
    Servicio desactivarServicio(Long idServicio);

    /** HU-18: desactiva, permitiendo forzarlo con confirmacion explicita. */
    Servicio desactivarServicio(Long idServicio, boolean confirmacion);
}