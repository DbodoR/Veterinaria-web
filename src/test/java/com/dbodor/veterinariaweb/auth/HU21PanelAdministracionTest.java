package com.dbodor.veterinariaweb.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-21 Panel de administracion.
 * Un test por criterio de aceptacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU21PanelAdministracionTest {

    @Autowired MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el administrador abre el panel sin pasos intermedios")
    void adminAbreElPanel() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/panel"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    @DisplayName("CA2 - el menu ofrece los cinco maestros, reservas y los dos reportes")
    void menuCompleto() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                // cinco maestros
                .andExpect(content().string(containsString("/admin/clientes")))
                .andExpect(content().string(containsString("/admin/veterinarios")))
                .andExpect(content().string(containsString("/admin/mascotas")))
                .andExpect(content().string(containsString("/admin/servicios")))
                .andExpect(content().string(containsString("/admin/productos")))
                // transaccional
                .andExpect(content().string(containsString("/admin/citas")))
                // dos reportes
                .andExpect(content().string(containsString("/admin/reportes/citas")))
                .andExpect(content().string(containsString("/admin/reportes/historias")));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMINISTRADOR")
    @DisplayName("CA3 - el panel trae el resumen del dia")
    void panelTraeResumen() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("citasHoy"))
                .andExpect(model().attributeExists("citasPendientes"))
                .andExpect(model().attributeExists("clientesActivos"));
    }

    @Test
    @WithMockUser(username = "cliente@test.com", roles = "CLIENTE")
    @DisplayName("CA4 - el cliente no entra al panel y vuelve a su inicio")
    void clienteNoEntraAlPanel() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cliente/dashboard"));
    }

    @Test
    @WithMockUser(username = "vet@test.com", roles = "VETERINARIO")
    @DisplayName("CA4 - el veterinario no entra al panel y vuelve a su agenda")
    void veterinarioNoEntraAlPanel() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/veterinario/agenda"));
    }

    @Test
    @DisplayName("CA5 - sin sesion, el panel manda al login")
    void sinSesionAlLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}