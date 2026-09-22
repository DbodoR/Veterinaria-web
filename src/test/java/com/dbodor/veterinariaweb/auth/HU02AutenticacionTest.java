package com.dbodor.veterinariaweb.auth;

import com.dbodor.veterinariaweb.model.EstadoUsuario;
import com.dbodor.veterinariaweb.model.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-02 Iniciar sesion en el sistema.
 * Un test por cada criterio de aceptacion, en el mismo orden de Jira.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU02AutenticacionTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiar() {
        usuarioRepository.deleteAll();
    }

    private Usuario crear(String correo, String documento, RolUsuario rol, EstadoUsuario estado) {
        Usuario u = new Usuario();
        u.setNombre("Usuario Prueba");
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode("Clave123"));
        u.setRol(rol);
        u.setEstado(estado);
        return usuarioRepository.save(u);
    }

    // ---- Criterio 1: redireccion segun el rol ----

    @Test
    @DisplayName("CA1 - el administrador entra a su panel")
    void administradorVaAlPanel() throws Exception {
        crear("admin@test.com", "1", RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("Clave123"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("CA1 - el cliente entra a sus citas")
    void clienteVaASusCitas() throws Exception {
        crear("cliente@test.com", "2", RolUsuario.CLIENTE, EstadoUsuario.ACTIVO);

        mockMvc.perform(formLogin("/login").user("cliente@test.com").password("Clave123"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("CA1 - el veterinario entra a su agenda")
    void veterinarioVaASuAgenda() throws Exception {
        crear("vet@test.com", "3", RolUsuario.VETERINARIO, EstadoUsuario.ACTIVO);

        mockMvc.perform(formLogin("/login").user("vet@test.com").password("Clave123"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/veterinario/agenda"));
    }

    // ---- Criterio 2: mensaje generico ----

    @Test
    @DisplayName("CA2 - contrasena incorrecta no revela cual dato fallo")
    void contrasenaIncorrectaMensajeGenerico() throws Exception {
        crear("admin@test.com", "1", RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("EstaNoEs"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=credenciales"));
    }

    @Test
    @DisplayName("CA2 - correo inexistente devuelve el mismo error que contrasena mala")
    void correoInexistenteMismoError() throws Exception {
        mockMvc.perform(formLogin("/login").user("noexiste@test.com").password("Clave123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=credenciales"));
    }

    // ---- Criterio 3: cuenta inactiva ----

    @Test
    @DisplayName("CA3 - la cuenta inactiva no puede ingresar y el motivo es distinto")
    void cuentaInactivaNoIngresa() throws Exception {
        crear("inactivo@test.com", "4", RolUsuario.CLIENTE, EstadoUsuario.INACTIVO);

        mockMvc.perform(formLogin("/login").user("inactivo@test.com").password("Clave123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=inactiva"));
    }

    // ---- Criterio 4: bloqueo tras cinco intentos ----

    @Test
    @DisplayName("CA4 - al quinto intento fallido la cuenta queda bloqueada")
    void bloqueoAlQuintoIntento() throws Exception {
        crear("admin@test.com", "1", RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(formLogin("/login").user("admin@test.com").password("mala"))
                    .andExpect(redirectedUrl("/login?error=credenciales"));
        }
        assertThat(usuarioRepository.findByCorreoIgnoreCase("admin@test.com").orElseThrow()
                .getBloqueadoHasta()).isNull();

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("mala"))
                .andExpect(unauthenticated());

        Usuario bloqueado = usuarioRepository.findByCorreoIgnoreCase("admin@test.com").orElseThrow();
        assertThat(bloqueado.getIntentosFallidos()).isEqualTo(5);
        assertThat(bloqueado.getBloqueadoHasta()).isNotNull();
    }

    @Test
    @DisplayName("CA4 - estando bloqueada, la contrasena correcta tampoco entra")
    void bloqueadaRechazaClaveCorrecta() throws Exception {
        crear("admin@test.com", "1", RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(formLogin("/login").user("admin@test.com").password("mala"));
        }

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("Clave123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=bloqueada"));
    }

    @Test
    @DisplayName("CA4 - un ingreso exitoso reinicia el contador de intentos")
    void ingresoExitosoReiniciaContador() throws Exception {
        crear("admin@test.com", "1", RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("mala"));
        mockMvc.perform(formLogin("/login").user("admin@test.com").password("mala"));
        assertThat(usuarioRepository.findByCorreoIgnoreCase("admin@test.com").orElseThrow()
                .getIntentosFallidos()).isEqualTo(2);

        mockMvc.perform(formLogin("/login").user("admin@test.com").password("Clave123"))
                .andExpect(authenticated());

        assertThat(usuarioRepository.findByCorreoIgnoreCase("admin@test.com").orElseThrow()
                .getIntentosFallidos()).isZero();
    }

    // ---- Criterio 5: pantallas internas protegidas ----

    @Test
    @DisplayName("CA5 - sin sesion, cualquier pantalla interna manda al login")
    void sinSesionRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/cliente/citas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("CA5 - la pantalla de login si es publica")
    void loginEsPublico() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }
}