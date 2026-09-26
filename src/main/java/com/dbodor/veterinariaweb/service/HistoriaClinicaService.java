package com.dbodor.veterinariaweb.service;

import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;

import java.util.List;
import java.util.Optional;

/**
 * HU-24 Historia clinica en PDF.
 * Reune los datos de la mascota, su dueno y sus atenciones.
 */
public interface HistoriaClinicaService {

    /** CA1: mascotas para elegir, con busqueda por mascota, dueno o documento. */
    List<Mascota> buscarMascotas(String texto);

    /** La mascota con su dueno; vacio si no existe (CA6). */
    Optional<Mascota> mascota(Long idMascota);

    /** CA2 y CA3: solo las citas de esta mascota, de la mas reciente a la mas antigua. */
    List<Cita> atenciones(Long idMascota);

    /** CA5: la historia completa como documento PDF. */
    byte[] generarPdf(Mascota mascota, List<Cita> atenciones);
}