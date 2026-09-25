package com.dbodor.veterinariaweb.enums;

/**
 * Estado de la cuenta. HU-04 exige baja logica: el cliente se
 * desactiva, nunca se elimina, para conservar el historial de citas.
 */
public enum EstadoUsuario {
    ACTIVO,
    INACTIVO
}