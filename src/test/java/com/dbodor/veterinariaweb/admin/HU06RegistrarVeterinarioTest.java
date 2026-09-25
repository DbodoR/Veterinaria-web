package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.model.Veterinario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.repository.VeterinarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-06 Registrar un veterinario.
 * Un test por cada criterio de aceptacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU06RegistrarVeterinarioTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    VeterinarioRepository veterinarioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiar() {
        veterinarioRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @org.junit.jupiter.api.AfterEach
    void limpiarAlTerminar() {
        veterinarioRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    /**
     * Crea un veterinario ya existente, para las pruebas de duplicidad y listado.
     */
    private Veterinario existente(String nombre, String documento, String correo,
            String tarjeta, String especialidad, EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode("Clave123"));
        u.setTelefono("3001112233");
        u.setRol(RolUsuario.VETERINARIO);
        u.setEstado(estado);
        usuarioRepository.save(u);

        Veterinario v = new Veterinario();
        v.setUsuario(u);
        v.setTarjetaProfesional(tarjeta);
        v.setEspecialidad(especialidad);
        v.setTarifa(new BigDecimal("50000"));
        return veterinarioRepository.save(v);
    }

    // ---------- CA1: alta correcta con cuenta asociada ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el veterinario queda activo y con su cuenta de usuario")
    void altaCorrecta() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Carlos Mejia")
                .param("documento", "9080706050")
                .param("tarjetaProfesional", "TP-11223")
                .param("especialidad", "Cirugia")
                .param("correo", "carlos@veterinaria.com")
                .param("telefono", "3149998877")
                .param("tarifa", "80000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/veterinarios"));

        Usuario cuenta = usuarioRepository.findByCorreoIgnoreCase("carlos@veterinaria.com").orElseThrow();
        assertThat(cuenta.getRol()).isEqualTo(RolUsuario.VETERINARIO);
        assertThat(cuenta.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(cuenta.getDocumento()).isEqualTo("9080706050");

        Veterinario guardado = veterinarioRepository.findByTarjetaProfesional("TP-11223").orElseThrow();
        assertThat(guardado.getUsuario().getIdUsuario()).isEqualTo(cuenta.getIdUsuario());
        assertThat(guardado.getEspecialidad()).isEqualTo("Cirugia");
        assertThat(guardado.getTarifa()).isEqualByComparingTo("80000");
        assertThat(guardado.estaActivo()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - si no se indica jornada, se aplica la de 08:00 a 17:00")
    void jornadaPorDefecto() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Diana Soto")
                .param("documento", "1112223334")
                .param("tarjetaProfesional", "TP-55667")
                .param("especialidad", "Dermatologia")
                .param("correo", "diana@veterinaria.com")
                .param("telefono", "3201234567")
                .param("tarifa", "60000"));

        Veterinario v = veterinarioRepository.findByTarjetaProfesional("TP-55667").orElseThrow();
        assertThat(v.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(v.getHoraFin()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la cuenta creada queda marcada para cambio de contrasena")
    void cuentaConPasswordTemporal() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Jorge Pena")
                .param("documento", "4443332221")
                .param("tarjetaProfesional", "TP-99001")
                .param("especialidad", "Odontologia")
                .param("correo", "jorge@veterinaria.com")
                .param("telefono", "3005554444")
                .param("tarifa", "70000"))
                .andExpect(flash().attributeExists("passwordTemporal"));

        Usuario cuenta = usuarioRepository.findByCorreoIgnoreCase("jorge@veterinaria.com").orElseThrow();
        assertThat(cuenta.getDebeCambiarPassword()).isTrue();
        assertThat(cuenta.getContrasena()).startsWith("$2");
    }

    // ---------- CA2: licencia duplicada ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - una tarjeta profesional repetida se rechaza")
    void tarjetaDuplicada() throws Exception {
        existente("Ana Vega", "111", "ana@veterinaria.com", "TP-REPETIDA", "Cirugia", EstadoUsuario.ACTIVO);

        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Otro Profesional")
                .param("documento", "222")
                .param("tarjetaProfesional", "TP-REPETIDA")
                .param("especialidad", "Cirugia")
                .param("correo", "otro@veterinaria.com")
                .param("telefono", "3001112233")
                .param("tarifa", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/veterinarios/formulario"))
                .andExpect(content().string(containsString("La tarjeta profesional ya esta registrada")));

        assertThat(usuarioRepository.existsByCorreoIgnoreCase("otro@veterinaria.com")).isFalse();
        assertThat(veterinarioRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un correo ya usado por otro usuario se rechaza")
    void correoDuplicado() throws Exception {
        existente("Ana Vega", "111", "ocupado@veterinaria.com", "TP-001", "Cirugia", EstadoUsuario.ACTIVO);

        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Otro Profesional")
                .param("documento", "222")
                .param("tarjetaProfesional", "TP-002")
                .param("especialidad", "Cirugia")
                .param("correo", "ocupado@veterinaria.com")
                .param("telefono", "3001112233")
                .param("tarifa", "50000"))
                .andExpect(content().string(containsString("El correo ya esta registrado")));

        assertThat(veterinarioRepository.count()).isEqualTo(1);
    }

    // ---------- CA3: especialidad obligatoria ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - sin especialidad el sistema exige el campo")
    void especialidadObligatoria() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Sin Especialidad")
                .param("documento", "333")
                .param("tarjetaProfesional", "TP-333")
                .param("especialidad", "")
                .param("correo", "sin@veterinaria.com")
                .param("telefono", "3001112233")
                .param("tarifa", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/veterinarios/formulario"))
                .andExpect(model().attributeHasFieldErrors("veterinarioForm", "especialidad"));

        assertThat(veterinarioRepository.count()).isZero();
        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - los demas campos obligatorios tambien se exigen")
    void camposObligatorios() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "")
                .param("documento", "")
                .param("tarjetaProfesional", "")
                .param("especialidad", "")
                .param("correo", "")
                .param("telefono", "")
                .param("tarifa", ""))
                .andExpect(model().attributeHasFieldErrors("veterinarioForm",
                        "nombre", "documento", "tarjetaProfesional", "especialidad", "correo", "telefono"));

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - una tarifa menor o igual a cero se rechaza")
    void tarifaInvalida() throws Exception {
        mockMvc.perform(post("/admin/veterinarios").with(csrf())
                .param("nombre", "Tarifa Cero")
                .param("documento", "444")
                .param("tarjetaProfesional", "TP-444")
                .param("especialidad", "General")
                .param("correo", "cero@veterinaria.com")
                .param("telefono", "3001112233")
                .param("tarifa", "0"))
                .andExpect(model().attributeHasFieldErrors("veterinarioForm", "tarifa"));

        assertThat(veterinarioRepository.count()).isZero();
    }

    // ---------- Listado del maestro ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - muestra los veterinarios con especialidad, tarifa y estado")
    void listado() throws Exception {
        existente("Ana Vega", "111", "ana@veterinaria.com", "TP-001", "Cirugia", EstadoUsuario.ACTIVO);
        existente("Luis Mora", "222", "luis@veterinaria.com", "TP-002", "Dermatologia", EstadoUsuario.INACTIVO);

        mockMvc.perform(get("/admin/veterinarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/veterinarios/lista"))
                .andExpect(model().attributeExists("veterinarios"))
                .andExpect(content().string(containsString("Ana Vega")))
                .andExpect(content().string(containsString("Dermatologia")))
                .andExpect(content().string(containsString("TP-001")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - el filtro por especialidad acota el resultado")
    void filtroPorEspecialidad() throws Exception {
        existente("Ana Vega", "111", "ana@veterinaria.com", "TP-001", "Cirugia", EstadoUsuario.ACTIVO);
        existente("Luis Mora", "222", "luis@veterinaria.com", "TP-002", "Dermatologia", EstadoUsuario.ACTIVO);

        mockMvc.perform(get("/admin/veterinarios").param("especialidad", "Cirugia"))
                .andExpect(content().string(containsString("Ana Vega")))
                .andExpect(content().string(not(containsString("Luis Mora"))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - la busqueda por nombre o tarjeta acota el resultado")
    void busquedaPorTexto() throws Exception {
        existente("Ana Vega", "111", "ana@veterinaria.com", "TP-001", "Cirugia", EstadoUsuario.ACTIVO);
        existente("Luis Mora", "222", "luis@veterinaria.com", "TP-002", "Dermatologia", EstadoUsuario.ACTIVO);

        mockMvc.perform(get("/admin/veterinarios").param("buscar", "Luis"))
                .andExpect(content().string(containsString("Luis Mora")))
                .andExpect(content().string(not(containsString("Ana Vega"))));

        mockMvc.perform(get("/admin/veterinarios").param("buscar", "TP-001"))
                .andExpect(content().string(containsString("Ana Vega")))
                .andExpect(content().string(not(containsString("Luis Mora"))));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al maestro de veterinarios")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/veterinarios"))
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/veterinarios"))
                .andExpect(redirectedUrl("/login"));
    }
}