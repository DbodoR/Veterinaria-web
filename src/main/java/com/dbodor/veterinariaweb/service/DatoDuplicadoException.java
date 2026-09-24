package com.dbodor.veterinariaweb.service;

/**
 * Se lanza cuando el correo o el documento ya pertenecen a otro usuario
 * (HU-01, criterios 2 y 3). Lleva el nombre del campo para que el
 * controlador pueda senalar el error justo en ese campo del formulario.
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
}