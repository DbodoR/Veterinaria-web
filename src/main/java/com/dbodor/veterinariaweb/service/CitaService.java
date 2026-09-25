package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.DetalleRecetaCita;

public interface CitaService{
    DetalleRecetaCita finalizarCita(Long idCita);
    Cita agendarCita(Cita cita);
    Double calcularTotalIngresos();
}
