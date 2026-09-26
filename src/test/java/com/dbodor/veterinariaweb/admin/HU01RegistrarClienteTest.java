package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-01 Registrar un cliente nuevo.
 * Un test por cada criterio de aceptacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU01RegistrarClienteTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiar() {
        usuarioRepository.deleteAll();
    }

    private Usuario cliente(String nombre, String documento, String correo, EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode("Clave123"));
        u.setTelefono("3001112233");
        u.setDireccion("Calle 1");
        u.setCiudad("Medellin");
        u.setRol(RolUsuario.CLIENTE);
        u.setEstado(estado);
        return usuarioRepository.save(u);
    }

    // ---------- CA1: alta correcta ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el cliente queda activo y con la fecha de registro del dia")
    void altaCorrecta() throws Exception {
        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "Laura Restrepo")
                        .param("documento", "1020304050")
                        .param("correo", "laura@correo.com")
                        .param("telefono", "3105558899")
                        .param("direccion", "Carrera 70 # 30-20")
                        .param("ciudad", "Medellin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/clientes"));

        Usuario guardado = usuarioRepository.findByCorreoIgnoreCase("laura@correo.com").orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Laura Restrepo");
        assertThat(guardado.getDocumento()).isEqualTo("1020304050");
        assertThat(guardado.getCiudad()).isEqualTo("Medellin");
        assertThat(guardado.getRol()).isEqualTo(RolUsuario.CLIENTE);
        assertThat(guardado.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(guardado.getFechaRegistro().toLocalDate()).isEqualTo(LocalDate.now());
    }

    // ---------- CA2: correo duplicado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un correo repetido se rechaza con su mensaje")
    void correoDuplicado() throws Exception {
        cliente("Cliente Uno", "111", "repetido@correo.com", EstadoUsuario.ACTIVO);

        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "Cliente Dos")
                        .param("documento", "222")
                        .param("correo", "repetido@correo.com")
                        .param("telefono", "3105558899")
                        .param("direccion", "Calle 2")
                        .param("ciudad", "Bogota"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes/formulario"))
                .andExpect(content().string(containsString("El correo ya esta registrado")));

        assertThat(usuarioRepository.existsByDocumento("222")).isFalse();
    }

    // ---------- CA3: documento duplicado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - un documento repetido se rechaza con su mensaje")
    void documentoDuplicado() throws Exception {
        cliente("Cliente Uno", "999888", "uno@correo.com", EstadoUsuario.ACTIVO);

        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "Cliente Dos")
                        .param("documento", "999888")
                        .param("correo", "dos@correo.com")
                        .param("telefono", "3105558899")
                        .param("direccion", "Calle 2")
                        .param("ciudad", "Bogota"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes/formulario"))
                .andExpect(content().string(containsString("El documento ya esta registrado")));

        assertThat(usuarioRepository.existsByCorreoIgnoreCase("dos@correo.com")).isFalse();
    }

    // ---------- CA4: campo obligatorio vacio ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - un campo obligatorio vacio impide guardar y no persiste nada")
    void campoObligatorioVacio() throws Exception {
        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "")
                        .param("documento", "333")
                        .param("correo", "sinnombre@correo.com")
                        .param("telefono", "3105558899")
                        .param("direccion", "Calle 3")
                        .param("ciudad", "Cali"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes/formulario"))
                .andExpect(model().attributeHasFieldErrors("clienteForm", "nombre"));

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - varios campos vacios se senalan todos a la vez")
    void variosCamposVacios() throws Exception {
        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "")
                        .param("documento", "")
                        .param("correo", "")
                        .param("telefono", "")
                        .param("direccion", "")
                        .param("ciudad", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("clienteForm",
                        "nombre", "documento", "correo", "telefono", "direccion", "ciudad"));

        assertThat(usuarioRepository.count()).isZero();
    }

    // ---------- CA5: contrasena temporal ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - se genera contrasena temporal y queda marcado el cambio obligatorio")
    void contrasenaTemporal() throws Exception {
        mockMvc.perform(post("/admin/clientes").with(csrf())
                        .param("nombre", "Pedro Gomez")
                        .param("documento", "444555")
                        .param("correo", "pedro@correo.com")
                        .param("telefono", "3115554433")
                        .param("direccion", "Calle 4")
                        .param("ciudad", "Pereira"))
                .andExpect(flash().attributeExists("passwordTemporal"));

        Usuario guardado = usuarioRepository.findByCorreoIgnoreCase("pedro@correo.com").orElseThrow();
        assertThat(guardado.getDebeCambiarPassword()).isTrue();
        assertThat(guardado.getContrasena()).isNotBlank();
        assertThat(guardado.getContrasena()).startsWith("$2");
    }

    // ---------- Listado del maestro ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - muestra los clientes con sus datos")
    void listadoMuestraClientes() throws Exception {
        cliente("Ana Torres", "123456", "ana@correo.com", EstadoUsuario.ACTIVO);
        cliente("Beto Ruiz", "654321", "beto@correo.com", EstadoUsuario.INACTIVO);

        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes/lista"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(containsString("654321")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - la busqueda filtra por nombre o documento")
    void busquedaFiltra() throws Exception {
        cliente("Ana Torres", "123456", "ana@correo.com", EstadoUsuario.ACTIVO);
        cliente("Beto Ruiz", "654321", "beto@correo.com", EstadoUsuario.ACTIVO);

        mockMvc.perform(get("/admin/clientes").param("buscar", "Ana"))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Beto Ruiz"))));

        mockMvc.perform(get("/admin/clientes").param("buscar", "654321"))
                .andExpect(content().string(containsString("Beto Ruiz")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Ana Torres"))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - el maestro solo trae clientes, no otros roles")
    void listadoSoloClientes() throws Exception {
        cliente("Ana Torres", "123456", "ana@correo.com", EstadoUsuario.ACTIVO);

        Usuario admin = new Usuario();
        admin.setNombre("Admin Sistema");
        admin.setDocumento("000");
        admin.setCorreo("admin@correo.com");
        admin.setContrasena(passwordEncoder.encode("Clave123"));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(admin);

        mockMvc.perform(get("/admin/clientes"))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Admin Sistema"))));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al maestro de clientes")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente/dashboard"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}