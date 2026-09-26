package com.dbodor.veterinariaweb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "mascotas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    private Long idMascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private String nombre;
    private String especie;
    private String raza;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    private String sexo;
    @Column(name = "peso_kg")
    private Double pesoKg;

    /** "ACTIVO" o "INACTIVO". Solo las activas se ofrecen al agendar citas. */
    private String estado;

    /**
     * HU-12 CA2: edad calculada a partir de la fecha de nacimiento.
     * En anos si tiene uno o mas; en meses si es menor. No se persiste.
     */
    public String getEdad() {
        if (fechaNacimiento == null) {
            return "Sin dato";
        }
        Period p = Period.between(fechaNacimiento, LocalDate.now());
        if (p.getYears() >= 1) {
            return p.getYears() + (p.getYears() == 1 ? " año" : " años");
        }
        if (p.getMonths() >= 1) {
            return p.getMonths() + (p.getMonths() == 1 ? " mes" : " meses");
        }
        return "Menos de un mes";
    }
}