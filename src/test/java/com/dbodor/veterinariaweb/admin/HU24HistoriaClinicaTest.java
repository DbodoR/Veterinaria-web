package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Cita;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.model.Veterinario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.repository.VeterinarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-24 Historia clinica en PDF.
 *
 * La historia reune los datos de la mascota, su dueno y todas sus
 * atenciones (citas). Los diagnosticos de HistoriaClinica quedan fuera
 * porque ninguna pantalla los registra todavia.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU24HistoriaClinicaTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CitaRepository citaRepository;
    @Autowired
    MascotaRepository mascotaRepository;
    @Autowired
    VeterinarioRepository veterinarioRepository;
    @Autowired
    ServicioRepository servicioRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

    private Mascota firulais;
    private Mascota michi;

    @BeforeEach
    void preparar() {
        limpiar();

        Usuario ana = usuario("Ana Torres", "1010101010", "ana@correo.com", RolUsuario.CLIENTE);
        Usuario luis = usuario("Luis Diaz", "2020202020", "luis@correo.com", RolUsuario.CLIENTE);
        firulais = mascota(ana, "Firulais", "Perro");
        michi = mascota(luis, "Michi", "Gato");

        Servicio vacunacion = servicio("Vacunacion antirrabica");
        Servicio limpieza = servicio("Limpieza dental");
        Veterinario laura = veterinario("Laura Gomez", "3030303030", "laura@veterinaria.com", "TP-1001");

        cita(firulais, vacunacion, laura, LocalDate.of(2026, 8, 5), "FINALIZADA");
        cita(firulais, vacunacion, laura, LocalDate.of(2026, 9, 5), "PROGRAMADA");
        cita(michi, limpieza, laura, LocalDate.of(2026, 8, 20), "FINALIZADA");
    }

    @AfterEach
    void limpiar() {
        citaRepository.deleteAll();
        mascotaRepository.deleteAll();
        veterinarioRepository.deleteAll();
        servicioRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // ---------- Datos de apoyo ----------

    private Usuario usuario(String nombre, String documento, String correo, RolUsuario rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena("no-se-usa-en-estas-pruebas");
        u.setTelefono("3001112233");
        u.setRol(rol);
        u.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(u);
    }

    private Mascota mascota(Usuario dueno, String nombre, String especie) {
        Mascota m = new Mascota();
        m.setUsuario(dueno);
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza("Criollo");
        m.setSexo("M");
        m.setFechaNacimiento(LocalDate.of(2022, 3, 15));
        m.setPesoKg(8.0);
        m.setEstado("ACTIVO");
        return mascotaRepository.save(m);
    }

    private Servicio servicio(String nombre) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion(nombre);
        s.setDuracionMinutos(30);
        s.setPrecioBase(50000.0);
        s.setEsConsultaVeterinaria(false);
        s.setEstado(EstadoServicio.ACTIVO);
        return servicioRepository.save(s);
    }

    private Veterinario veterinario(String nombre, String documento, String correo, String tarjeta) {
        Usuario u = usuario(nombre, documento, correo, RolUsuario.VETERINARIO);
        Veterinario v = new Veterinario();
        v.setUsuario(u);
        v.setTarjetaProfesional(tarjeta);
        v.setEspecialidad("Medicina general");
        v.setTarifa(new BigDecimal("40000"));
        v.setHoraInicio(LocalTime.of(8, 0));
        v.setHoraFin(LocalTime.of(17, 0));
        return veterinarioRepository.save(v);
    }

    private void cita(Mascota m, Servicio s, Veterinario v, LocalDate fecha, String estado) {
        Cita c = new Cita();
        c.setMascota(m);
        c.setServicio(s);
        c.setVeterinario(v);
        c.setFechaCita(fecha);
        c.setHoraCita(LocalTime.of(10, 0));
        c.setEstado(estado);
        c.setCostoTotal(90000.0);
        c.setCreatedAt(LocalDateTime.of(2026, 8, 1, 8, 0));
        citaRepository.save(c);
    }

    // ---------- CA1: seleccion de la mascota ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la pantalla lista las mascotas para elegir la historia")
    void listaMascotas() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reportes/historias"))
                .andExpect(model().attribute("mascotas", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la busqueda filtra por mascota, dueno o documento")
    void buscaMascota() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias").param("buscar", "luis"))
                .andExpect(model().attribute("mascotas", hasSize(1)));
    }

    // ---------- CA2 y CA3: contenido de la historia ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - la historia muestra los datos de la mascota y de su dueno")
    void muestraDatosDeMascotaYDueno() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias/" + firulais.getIdMascota()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reportes/historia-detalle"))
                .andExpect(content().string(containsString("Firulais")))
                .andExpect(content().string(containsString("Perro")))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(containsString("1010101010")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - la historia lista las atenciones con fecha, servicio, veterinario y estado")
    void muestraAtenciones() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias/" + firulais.getIdMascota()))
                .andExpect(model().attribute("atenciones", hasSize(2)))
                .andExpect(content().string(containsString("2026-08-05")))
                .andExpect(content().string(containsString("Vacunacion antirrabica")))
                .andExpect(content().string(containsString("Laura Gomez")))
                .andExpect(content().string(containsString("FINALIZADA")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - solo aparecen las atenciones de la mascota consultada")
    void soloAtencionesDeLaMascota() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias/" + firulais.getIdMascota()))
                .andExpect(content().string(not(containsString("Limpieza dental"))));
    }

    // ---------- CA4: sin atenciones ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - una mascota sin atenciones lo informa")
    void sinAtenciones() throws Exception {
        citaRepository.deleteAll();

        mockMvc.perform(get("/admin/reportes/historias/" + firulais.getIdMascota()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("atenciones", hasSize(0)))
                .andExpect(content().string(containsString("La mascota no tiene atenciones registradas")));
    }

    // ---------- CA5: descarga en PDF ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - la descarga genera un documento PDF con nombre de archivo")
    void descargaPdf() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/admin/reportes/historias/" + firulais.getIdMascota() + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("historia-clinica-")))
                .andReturn();

        byte[] cuerpo = resultado.getResponse().getContentAsByteArray();
        assertThat(cuerpo.length).isGreaterThan(500);
        assertThat(new String(cuerpo, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    // ---------- CA6: mascota inexistente ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA6 - una mascota inexistente vuelve al listado")
    void mascotaInexistente() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias/999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reportes/historias"));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra a las historias clinicas del administrador")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias"))
                .andExpect(redirectedUrl("/cliente/dashboard"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/reportes/historias"))
                .andExpect(redirectedUrl("/login"));
    }
}