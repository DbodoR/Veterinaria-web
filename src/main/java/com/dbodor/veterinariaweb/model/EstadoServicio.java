package com.dbodor.veterinariaweb.model;

/**
 * Estado de un servicio del catalogo. HU-18 exige baja logica: un servicio
 * se desactiva, nunca se elimina, para que las citas anteriores conserven
 * su referencia.
 */
public enum EstadoServicio {
    ACTIVO,
    INACTIVO
}