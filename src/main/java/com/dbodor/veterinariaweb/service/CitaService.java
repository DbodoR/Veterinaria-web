package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.DetalleRecetaCita;

public interface CitaService{
    DetalleRecetaCita finalizarCita(Long idCita);
}
