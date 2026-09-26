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
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-26 Reporte de citas e ingresos por periodo.
 *
 * Las citas se insertan directamente en la base, con fechas de agosto y
 * septiembre de 2026, para que el reporte tenga datos historicos fijos que
 * no dependan del dia en que se ejecuten las pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU26ReporteCitasTest {

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

    private Veterinario laura;
    private Veterinario pedro;
    private Servicio consulta;

    @BeforeEach
    void preparar() {
        limpiar();

        Usuario ana = usuario("Ana Torres", "1010101010", "ana@correo.com", RolUsuario.CLIENTE);
        Mascota firulais = mascota(ana, "Firulais");
        consulta = servicio("Consulta general");
        laura = veterinario("Laura Gomez", "2020202020", "laura@veterinaria.com", "TP-1001");
        pedro = veterinario("Pedro Ruiz", "3030303030", "pedro@veterinaria.com", "TP-1002");

        // Agosto: 4 citas (2 finalizadas, 1 cancelada, 1 programada)
        cita(firulais, laura, LocalDate.of(2026, 8, 5), "FINALIZADA", 100000.0);
        cita(firulais, laura, LocalDate.of(2026, 8, 10), "CANCELADA", 100000.0);
        cita(firulais, pedro, LocalDate.of(2026, 8, 15), "FINALIZADA", 80000.0);
        cita(firulais, laura, LocalDate.of(2026, 8, 20), "PROGRAMADA", 100000.0);
        // Septiembre: fuera del rango de agosto
        cita(firulais, laura, LocalDate.of(2026, 9, 10), "FINALIZADA", 100000.0);
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

    private Mascota mascota(Usuario dueno, String nombre) {
        Mascota m = new Mascota();
        m.setUsuario(dueno);
        m.setNombre(nombre);
        m.setEspecie("Perro");
        m.setSexo("M");
        m.setEstado("ACTIVO");
        return mascotaRepository.save(m);
    }

    private Servicio servicio(String nombre) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion(nombre);
        s.setDuracionMinutos(60);
        s.setPrecioBase(60000.0);
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

    private void cita(Mascota mascota, Veterinario vet, LocalDate fecha, String estado, double valor) {
        Cita c = new Cita();
        c.setMascota(mascota);
        c.setServicio(consulta);
        c.setVeterinario(vet);
        c.setFechaCita(fecha);
        c.setHoraCita(LocalTime.of(9, 0));
        c.setEstado(estado);
        c.setCostoTotal(valor);
        c.setCreatedAt(LocalDateTime.of(2026, 8, 1, 8, 0));
        citaRepository.save(c);
    }

    private ResultActions generar(String desde, String hasta) throws Exception {
        return mockMvc.perform(get("/admin/reportes/citas").param("desde", desde).param("hasta", hasta));
    }

    private ResultActions generar(String desde, String hasta, Veterinario vet) throws Exception {
        return mockMvc.perform(get("/admin/reportes/citas")
                .param("desde", desde).param("hasta", hasta)
                .param("idVeterinario", vet.getIdVeterinario().toString()));
    }

    // ---------- Pantalla inicial ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Pantalla - sin fechas muestra el formulario y todavia no genera reporte")
    void sinFechasNoGeneraReporte() throws Exception {
        mockMvc.perform(get("/admin/reportes/citas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reportes/citas"))
                .andExpect(model().attributeDoesNotExist("reporte"));
    }

    // ---------- CA1: citas del rango con todas sus columnas ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el reporte trae solo las citas dentro del rango")
    void traeSoloCitasDelRango() throws Exception {
        generar("2026-08-01", "2026-08-31")
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", hasProperty("citas", hasSize(4))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - las dos fechas del rango son inclusivas")
    void fechasInclusivas() throws Exception {
        generar("2026-08-05", "2026-08-05")
                .andExpect(model().attribute("reporte", hasProperty("citas", hasSize(1))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - cada fila muestra fecha, cliente, mascota, servicio, veterinario, estado y valor")
    void filaMuestraSusColumnas() throws Exception {
        generar("2026-08-05", "2026-08-05")
                .andExpect(content().string(containsString("2026-08-05")))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(containsString("Firulais")))
                .andExpect(content().string(containsString("Consulta general")))
                .andExpect(content().string(containsString("Laura Gomez")))
                .andExpect(content().string(containsString("FINALIZADA")))
                .andExpect(content().string(containsString("100.000")));
    }

    // ---------- CA2: filtro por veterinario ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - el filtro por veterinario deja solo sus citas")
    void filtraPorVeterinario() throws Exception {
        generar("2026-08-01", "2026-08-31", pedro)
                .andExpect(model().attribute("reporte", hasProperty("citas", hasSize(1))));
    }

    // ---------- CA3: resumen ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - el resumen cuenta total, finalizadas, canceladas e ingresos de las finalizadas")
    void resumenDelPeriodo() throws Exception {
        generar("2026-08-01", "2026-08-31")
                .andExpect(model().attribute("reporte", hasProperty("total", is(4L))))
                .andExpect(model().attribute("reporte", hasProperty("finalizadas", is(2L))))
                .andExpect(model().attribute("reporte", hasProperty("canceladas", is(1L))))
                .andExpect(model().attribute("reporte", hasProperty("ingresos", is(180000.0))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - el resumen respeta el filtro por veterinario")
    void resumenConFiltro() throws Exception {
        generar("2026-08-01", "2026-08-31", laura)
                .andExpect(model().attribute("reporte", hasProperty("total", is(3L))))
                .andExpect(model().attribute("reporte", hasProperty("finalizadas", is(1L))))
                .andExpect(model().attribute("reporte", hasProperty("canceladas", is(1L))))
                .andExpect(model().attribute("reporte", hasProperty("ingresos", is(100000.0))));
    }

    // ---------- CA4: el historico no depende del estado actual ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - las citas de un veterinario y un servicio desactivados siguen en el historico")
    void desactivadosSiguenEnHistorico() throws Exception {
        laura.getUsuario().setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(laura.getUsuario());
        consulta.setEstado(EstadoServicio.INACTIVO);
        servicioRepository.save(consulta);

        generar("2026-08-01", "2026-08-31")
                .andExpect(model().attribute("reporte", hasProperty("citas", hasSize(4))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - el filtro ofrece tambien a los veterinarios inactivos")
    void filtroIncluyeVeterinariosInactivos() throws Exception {
        laura.getUsuario().setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(laura.getUsuario());

        mockMvc.perform(get("/admin/reportes/citas"))
                .andExpect(model().attribute("veterinarios", hasSize(2)));
    }

    // ---------- CA5: rango invalido ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - una fecha inicial posterior a la final se rechaza y se explica")
    void rangoInvertidoSeRechaza() throws Exception {
        generar("2026-08-31", "2026-08-01")
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("reporte"))
                .andExpect(content().string(containsString("La fecha inicial no puede ser posterior a la final")));
    }

    // ---------- CA6: periodo sin datos ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA6 - un periodo sin citas informa que no hay datos")
    void periodoSinCitas() throws Exception {
        generar("2025-01-01", "2025-01-31")
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", hasProperty("citas", hasSize(0))))
                .andExpect(content().string(containsString("No hay citas para ese periodo")));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al reporte")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/reportes/citas"))
                .andExpect(redirectedUrl("/cliente/dashboard"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/reportes/citas"))
                .andExpect(redirectedUrl("/login"));
    }
}