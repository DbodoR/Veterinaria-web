package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Mascota;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.CitaRepository;
import com.dbodor.veterinariaweb.repository.MascotaRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-12 Consultar las mascotas registradas.
 *
 * Listado del maestro de mascotas del panel de administracion.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU12ConsultarMascotasTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CitaRepository citaRepository;
    @Autowired
    MascotaRepository mascotaRepository;
    @Autowired
    VeterinarioRepository veterinarioRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

    @BeforeEach
    void preparar() {
        limpiar();

        Usuario ana = usuario("Ana Torres", "1010101010", "ana@correo.com");
        Usuario luis = usuario("Luis Diaz", "2020202020", "luis@correo.com");

        mascota(ana, "Firulais", "Perro", "Criollo",
                LocalDate.now().minusYears(3).minusDays(10), "ACTIVO");
        mascota(luis, "Michi", "Gato", "Siames",
                LocalDate.now().minusMonths(5).minusDays(3), "INACTIVO");
    }

    @AfterEach
    void limpiar() {
        citaRepository.deleteAll();
        mascotaRepository.deleteAll();
        veterinarioRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // ---------- Datos de apoyo ----------

    private Usuario usuario(String nombre, String documento, String correo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setDocumento(documento);
        u.setCorreo(correo);
        u.setContrasena("no-se-usa-en-estas-pruebas");
        u.setTelefono("3001112233");
        u.setRol(RolUsuario.CLIENTE);
        u.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepository.save(u);
    }

    private void mascota(Usuario dueno, String nombre, String especie, String raza,
                         LocalDate nacimiento, String estado) {
        Mascota m = new Mascota();
        m.setUsuario(dueno);
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza(raza);
        m.setSexo("M");
        m.setFechaNacimiento(nacimiento);
        m.setPesoKg(5.0);
        m.setEstado(estado);
        mascotaRepository.save(m);
    }

    // ---------- CA1: listado con busqueda ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el listado muestra todas las mascotas registradas")
    void listadoMuestraTodas() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mascotas/lista"))
                .andExpect(model().attribute("mascotas", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la busqueda filtra por el nombre de la mascota")
    void buscaPorNombreDeMascota() throws Exception {
        mockMvc.perform(get("/admin/mascotas").param("buscar", "firu"))
                .andExpect(model().attribute("mascotas", hasSize(1)))
                .andExpect(model().attribute("mascotas", hasItem(hasProperty("nombre", is("Firulais")))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la busqueda filtra por el nombre del dueno")
    void buscaPorNombreDelDueno() throws Exception {
        mockMvc.perform(get("/admin/mascotas").param("buscar", "luis"))
                .andExpect(model().attribute("mascotas", hasSize(1)))
                .andExpect(model().attribute("mascotas", hasItem(hasProperty("nombre", is("Michi")))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la busqueda filtra por el documento del dueno")
    void buscaPorDocumentoDelDueno() throws Exception {
        mockMvc.perform(get("/admin/mascotas").param("buscar", "1010"))
                .andExpect(model().attribute("mascotas", hasSize(1)))
                .andExpect(model().attribute("mascotas", hasItem(hasProperty("nombre", is("Firulais")))));
    }

    // ---------- CA2: datos de cada fila ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - cada fila muestra nombre, especie, raza, dueno y estado")
    void filaMuestraSusDatos() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(content().string(containsString("Firulais")))
                .andExpect(content().string(containsString("Perro")))
                .andExpect(content().string(containsString("Criollo")))
                .andExpect(content().string(containsString("Ana Torres")))
                .andExpect(content().string(containsString("ACTIVO")))
                .andExpect(content().string(containsString("INACTIVO")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - la edad se calcula en anos a partir de la fecha de nacimiento")
    void edadEnAnios() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(content().string(containsString("3 años")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - una mascota de menos de un ano muestra la edad en meses")
    void edadEnMeses() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(content().string(containsString("5 meses")));
    }

    // ---------- CA3: sin mascotas invita a registrar la primera ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - sin mascotas el sistema invita a registrar la primera")
    void sinMascotasInvitaARegistrar() throws Exception {
        mascotaRepository.deleteAll();

        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay mascotas registradas")))
                .andExpect(content().string(containsString("/admin/mascotas/nueva")));
    }

    // ---------- CA4: control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("CA4 - un cliente no entra al maestro de mascotas")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("CA4 - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/mascotas"))
                .andExpect(redirectedUrl("/login"));
    }
}