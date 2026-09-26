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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-25 Agendar una cita.
 *
 * Pantalla transaccional del panel de administracion. Cada criterio de
 * aceptacion se traduce en uno o mas metodos de prueba.
 *
 * Las citas y las mascotas tienen clave foranea hacia usuarios, asi que
 * se eliminan antes que ellos, tanto al empezar como al terminar, para no
 * contaminar las demas clases de prueba que comparten la base H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU25AgendarCitaTest {

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

    /** Una semana adelante, para que ninguna prueba dependa de la hora actual. */
    private final LocalDate fechaFutura = LocalDate.now().plusDays(7);

    private Usuario cliente;
    private Mascota firulais;
    private Servicio consulta;
    private Veterinario doctora;

    @BeforeEach
    void preparar() {
        limpiar();

        cliente = usuario("Ana Torres", "1010101010", "ana@correo.com", RolUsuario.CLIENTE, EstadoUsuario.ACTIVO);
        firulais = mascota(cliente, "Firulais", "ACTIVO");
        consulta = servicio("Consulta general", 60, 60000.0, EstadoServicio.ACTIVO);
        doctora = veterinario("Laura Gomez", "2020202020", "laura@veterinaria.com", "TP-1001",
                EstadoUsuario.ACTIVO, "40000");
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

    private Usuario usuario(String nombre, String documento, String correo,
                            RolUsuario rol, EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena("no-se-usa-en-estas-pruebas");
        u.setTelefono("3001112233");
        u.setRol(rol);
        u.setEstado(estado);
        return usuarioRepository.save(u);
    }

    private Mascota mascota(Usuario dueno, String nombre, String estado) {
        Mascota m = new Mascota();
        m.setUsuario(dueno);
        m.setNombre(nombre);
        m.setEspecie("Perro");
        m.setRaza("Criollo");
        m.setSexo("M");
        m.setEstado(estado);
        return mascotaRepository.save(m);
    }

    private Servicio servicio(String nombre, int minutos, double precio, EstadoServicio estado) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion(nombre);
        s.setDuracionMinutos(minutos);
        s.setPrecioBase(precio);
        s.setEsConsultaVeterinaria(false);
        s.setEstado(estado);
        return servicioRepository.save(s);
    }

    private Veterinario veterinario(String nombre, String documento, String correo, String tarjeta,
                                    EstadoUsuario estado, String tarifa) {
        Usuario u = usuario(nombre, documento, correo, RolUsuario.VETERINARIO, estado);
        Veterinario v = new Veterinario();
        v.setUsuario(u);
        v.setTarjetaProfesional(tarjeta);
        v.setEspecialidad("Medicina general");
        v.setTarifa(new BigDecimal(tarifa));
        v.setHoraInicio(LocalTime.of(8, 0));
        v.setHoraFin(LocalTime.of(17, 0));
        return veterinarioRepository.save(v);
    }

    /** Envia el formulario de agendamiento con los datos indicados. */
    private org.springframework.test.web.servlet.ResultActions agendar(Mascota m, Servicio s, Veterinario v,
                                                                       LocalDate fecha, String hora) throws Exception {
        return mockMvc.perform(post("/admin/citas").with(csrf())
                .param("idMascota", m.getIdMascota().toString())
                .param("idServicio", s.getIdServicio().toString())
                .param("idVeterinario", v.getIdVeterinario().toString())
                .param("fecha", fecha.toString())
                .param("hora", hora));
    }

    // ---------- CA1: solo se ofrecen servicios y veterinarios activos ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el formulario ofrece solo servicios activos")
    void soloServiciosActivos() throws Exception {
        servicio("Peluqueria retirada", 30, 25000.0, EstadoServicio.INACTIVO);

        mockMvc.perform(get("/admin/citas/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/citas/formulario"))
                .andExpect(model().attribute("servicios", hasSize(1)))
                .andExpect(model().attribute("servicios", hasItem(hasProperty("nombre", is("Consulta general")))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el formulario ofrece solo veterinarios activos")
    void soloVeterinariosActivos() throws Exception {
        veterinario("Pedro Ruiz", "3030303030", "pedro@veterinaria.com", "TP-1002",
                EstadoUsuario.INACTIVO, "40000");

        mockMvc.perform(get("/admin/citas/nueva"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("veterinarios", hasSize(1)))
                .andExpect(model().attribute("veterinarios", hasItem(hasProperty("nombre", is("Laura Gomez")))));
    }

    // ---------- CA2: al elegir el cliente, solo sus mascotas activas ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - al elegir un cliente se ofrecen solo sus mascotas activas")
    void soloMascotasActivasDelCliente() throws Exception {
        mascota(cliente, "Michi", "INACTIVO");
        Usuario otro = usuario("Luis Diaz", "4040404040", "luis@correo.com", RolUsuario.CLIENTE, EstadoUsuario.ACTIVO);
        mascota(otro, "Rocky", "ACTIVO");

        mockMvc.perform(get("/admin/citas/nueva").param("idCliente", cliente.getIdUsuario().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mascotas", hasSize(1)))
                .andExpect(model().attribute("mascotas", hasItem(hasProperty("nombre", is("Firulais")))));
    }

    // ---------- CA3: la cita valida queda programada con el valor congelado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - una cita valida se guarda y redirige al listado")
    void citaValidaSeGuarda() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "09:00")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/citas"));

        assertThat(citaRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - la cita queda PROGRAMADA, con fecha de creacion y valor = precio del servicio + tarifa")
    void citaQuedaProgramadaConValor() throws Exception {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        agendar(firulais, consulta, doctora, fechaFutura, "09:00");

        Cita guardada = citaRepository.findAll().getFirst();
        assertThat(guardada.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(guardada.getFechaCita()).isEqualTo(fechaFutura);
        assertThat(guardada.getHoraCita()).isEqualTo(LocalTime.of(9, 0));
        assertThat(guardada.getCreatedAt()).isAfter(antes);
        assertThat(guardada.getCostoTotal()).isEqualTo(100000.0);
    }

    // ---------- CA4: no se permiten franjas solapadas ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - un servicio de 60 min a las 09:00 bloquea al veterinario hasta las 10:00")
    void franjaSolapadaSeRechaza() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "09:00");

        agendar(firulais, consulta, doctora, fechaFutura, "09:30")
                .andExpect(status().isOk())
                .andExpect(view().name("admin/citas/formulario"))
                .andExpect(content().string(containsString("El veterinario ya tiene una cita asignada en ese rango horario")));

        assertThat(citaRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - la franja que empieza justo cuando termina la anterior se acepta")
    void franjaContiguaSeAcepta() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "09:00");

        agendar(firulais, consulta, doctora, fechaFutura, "10:00")
                .andExpect(status().is3xxRedirection());

        assertThat(citaRepository.count()).isEqualTo(2);
    }

    // ---------- CA5: no se agenda en el pasado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - una fecha ya pasada se rechaza")
    void fechaPasadaSeRechaza() throws Exception {
        agendar(firulais, consulta, doctora, LocalDate.now().minusDays(1), "10:00")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La fecha y hora de la cita ya pasaron")));

        assertThat(citaRepository.count()).isZero();
    }

    // ---------- CA6: la cita cae dentro del horario del veterinario ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA6 - una hora anterior al inicio de la jornada se rechaza")
    void antesDeLaJornadaSeRechaza() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "07:00")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La cita queda fuera del horario del veterinario")));

        assertThat(citaRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA6 - una cita que termina despues del fin de la jornada se rechaza")
    void terminaDespuesDeLaJornadaSeRechaza() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "16:30")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La cita queda fuera del horario del veterinario")));

        assertThat(citaRepository.count()).isZero();
    }

    // ---------- CA7: la cita aparece en el listado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA7 - la cita agendada aparece en el listado con mascota, servicio, veterinario y estado")
    void citaApareceEnListado() throws Exception {
        agendar(firulais, consulta, doctora, fechaFutura, "09:00");

        mockMvc.perform(get("/admin/citas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/citas/lista"))
                .andExpect(content().string(containsString("Firulais")))
                .andExpect(content().string(containsString("Consulta general")))
                .andExpect(content().string(containsString("Laura Gomez")))
                .andExpect(content().string(containsString("PROGRAMADA")));
    }

    // ---------- Control de acceso y datos manipulados ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra a la pantalla de reservas del administrador")
    void clienteNoAccede() throws Exception {
        mockMvc.perform(get("/admin/citas"))
                .andExpect(redirectedUrl("/cliente/dashboard"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/citas"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - un servicio inactivo enviado a mano no se puede agendar")
    void servicioInactivoSeRechaza() throws Exception {
        Servicio retirado = servicio("Peluqueria retirada", 30, 25000.0, EstadoServicio.INACTIVO);

        agendar(firulais, retirado, doctora, fechaFutura, "09:00")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El servicio seleccionado no esta disponible")));

        assertThat(citaRepository.count()).isZero();
    }
}