package com.dbodor.veterinariaweb.service;

/**
 * Se lanza cuando un dato que debe ser unico ya pertenece a otro registro
 * (HU-01 criterios 2 y 3, HU-06 criterio 2). Lleva el nombre del campo para
 * que el controlador pueda senalar el error justo en ese campo del formulario.
 */
public class DatoDuplicadoException extends RuntimeException {

    private final String campo;

    public DatoDuplicadoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }

    public static DatoDuplicadoException correo() {
        return new DatoDuplicadoException("correo", "El correo ya esta registrado");
    }

    public static DatoDuplicadoException documento() {
        return new DatoDuplicadoException("documento", "El documento ya esta registrado");
    }

    public static DatoDuplicadoException tarjetaProfesional() {
        return new DatoDuplicadoException("tarjetaProfesional",
                "La tarjeta profesional ya esta registrada");
    }
}