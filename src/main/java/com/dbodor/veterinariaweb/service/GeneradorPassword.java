package com.dbodor.veterinariaweb.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera las contrasenas temporales que el administrador entrega al crear
 * una cuenta (HU-01 y HU-06). Se omiten los caracteres ambiguos (O, 0, l, 1)
 * porque la clave suele dictarse de viva voz.
 */
@Component
public class GeneradorPassword {

    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO = 10;

    private final SecureRandom random = new SecureRandom();

    public String temporal() {
        StringBuilder sb = new StringBuilder(LARGO);
        for (int i = 0; i < LARGO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}