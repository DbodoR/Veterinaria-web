package com.dbodor.veterinariaweb.model;

/**
 * Roles del sistema. Son los tres que define HU-02:
 * el cliente entra a sus citas, el veterinario a su agenda
 * y el administrador a su panel.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    VETERINARIO,
    CLIENTE
}