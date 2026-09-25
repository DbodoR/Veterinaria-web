package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.Servicio;

import java.util.List;

public interface ServicioService {
    List<Servicio> listarServiciosParaCliente();
    List<Servicio> listarTodosLosServicios();
    Servicio guardarServicio(Servicio servicio);

    Servicio actualizarPrecio(Long idServicio, Double nuevoPrecio);
}
