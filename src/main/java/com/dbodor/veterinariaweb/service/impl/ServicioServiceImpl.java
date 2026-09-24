package com.dbodor.veterinariaweb.service.impl;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.service.ServicioService;

import java.util.List;

public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository){
        this.servicioRepository = servicioRepository;
    }
    @Override
    public List<Servicio> listarServiciosParaCliente() {
        return servicioRepository.findByEstado(EstadoServicio.ACTIVO);
    }

    @Override
    public List<Servicio> listarTodosLosServicios() {
        return servicioRepository.findAll();
    }
}
