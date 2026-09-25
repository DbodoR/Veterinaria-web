package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoServicio;
import com.dbodor.veterinariaweb.model.Servicio;
import com.dbodor.veterinariaweb.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-16 Crear un servicio.
 * Incluye las validaciones de duracion que exige HU-20.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU16CrearServicioTest {

    @Autowired MockMvc mockMvc;
    @Autowired ServicioRepository servicioRepository;

    @BeforeEach
    void limpiar() {
        servicioRepository.deleteAll();
    }

    private Servicio existente(String nombre, EstadoServicio estado) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDescripcion("Descripcion de prueba");
        s.setDuracionMinutos(30);
        s.setPrecioBase(50000.0);
        s.setEstado(estado);
        return servicioRepository.save(s);
    }

    // ---------- CA1: alta correcta ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el servicio queda disponible en estado activo")
    void altaCorrecta() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Radiografia")
                        .param("descripcion", "Toma de placa radiografica")
                        .param("duracionMinutos", "30")
                        .param("precioBase", "95000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        Servicio guardado = servicioRepository.findByNombreIgnoreCase("Radiografia").orElseThrow();
        assertThat(guardado.getEstado()).isEqualTo(EstadoServicio.ACTIVO);
        assertThat(guardado.getDuracionMinutos()).isEqualTo(30);
        assertThat(guardado.getPrecioBase()).isEqualTo(95000.0);
        assertThat(guardado.estaActivo()).isTrue();
    }

    // ---------- CA2: nombre duplicado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un nombre de servicio repetido se rechaza")
    void nombreDuplicado() throws Exception {
        existente("Vacunacion", EstadoServicio.ACTIVO);

        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Vacunacion")
                        .param("descripcion", "Otra cosa")
                        .param("duracionMinutos", "15")
                        .param("precioBase", "40000"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/servicios/formulario"))
                .andExpect(content().string(containsString("Ya existe un servicio con ese nombre")));

        assertThat(servicioRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - la comparacion de nombres ignora mayusculas")
    void nombreDuplicadoIgnorandoMayusculas() throws Exception {
        existente("Vacunacion", EstadoServicio.ACTIVO);

        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "VACUNACION")
                        .param("descripcion", "Otra cosa")
                        .param("duracionMinutos", "15")
                        .param("precioBase", "40000"))
                .andExpect(content().string(containsString("Ya existe un servicio con ese nombre")));

        assertThat(servicioRepository.count()).isEqualTo(1);
    }

    // ---------- CA3: precio invalido ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - un precio de cero se rechaza")
    void precioCero() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Servicio gratis")
                        .param("descripcion", "Prueba")
                        .param("duracionMinutos", "30")
                        .param("precioBase", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("servicioForm", "precioBase"));

        assertThat(servicioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - un precio negativo se rechaza")
    void precioNegativo() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Servicio raro")
                        .param("descripcion", "Prueba")
                        .param("duracionMinutos", "30")
                        .param("precioBase", "-100"))
                .andExpect(model().attributeHasFieldErrors("servicioForm", "precioBase"));

        assertThat(servicioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - los campos obligatorios se exigen")
    void camposObligatorios() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "")
                        .param("descripcion", "")
                        .param("duracionMinutos", "")
                        .param("precioBase", ""))
                .andExpect(model().attributeHasFieldErrors("servicioForm",
                        "nombre", "duracionMinutos", "precioBase"));

        assertThat(servicioRepository.count()).isZero();
    }

    // ---------- HU-20: duracion en bloques validos ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("HU-20 - una duracion que no es multiplo de quince se rechaza")
    void duracionNoMultiploDeQuince() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Duracion rara")
                        .param("descripcion", "Prueba")
                        .param("duracionMinutos", "20")
                        .param("precioBase", "50000"))
                .andExpect(model().attributeHasFieldErrors("servicioForm", "duracionEnBloquesValidos"));

        assertThat(servicioRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("HU-20 - una duracion menor a quince minutos se rechaza")
    void duracionDemasiadoCorta() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Muy corto")
                        .param("descripcion", "Prueba")
                        .param("duracionMinutos", "5")
                        .param("precioBase", "50000"))
                .andExpect(model().attributeHasFieldErrors("servicioForm", "duracionMinutos"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("HU-20 - una duracion mayor a ciento ochenta minutos se rechaza")
    void duracionDemasiadoLarga() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                        .param("nombre", "Muy largo")
                        .param("descripcion", "Prueba")
                        .param("duracionMinutos", "240")
                        .param("precioBase", "50000"))
                .andExpect(model().attributeHasFieldErrors("servicioForm", "duracionMinutos"));
    }

    // ---------- CA4: disponible para agendar ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - el servicio creado queda entre los activos para agendar")
    void disponibleParaAgendar() throws Exception {
        mockMvc.perform(post("/admin/servicios").with(csrf())
                .param("nombre", "Ecografia")
                .param("descripcion", "Estudio ecografico")
                .param("duracionMinutos", "45")
                .param("precioBase", "120000"));

        assertThat(servicioRepository.findByEstadoOrderByNombreAsc(EstadoServicio.ACTIVO))
                .extracting(Servicio::getNombre)
                .contains("Ecografia");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - el listado muestra duracion y precio")
    void listadoMuestraDuracionYPrecio() throws Exception {
        existente("Consulta general", EstadoServicio.ACTIVO);

        mockMvc.perform(get("/admin/servicios"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/servicios/lista"))
                .andExpect(model().attributeExists("servicios"))
                .andExpect(content().string(containsString("Consulta general")))
                .andExpect(content().string(containsString("30")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - la busqueda por nombre acota el resultado")
    void busquedaFiltra() throws Exception {
        existente("Consulta general", EstadoServicio.ACTIVO);
        existente("Peluqueria", EstadoServicio.ACTIVO);

        mockMvc.perform(get("/admin/servicios").param("buscar", "Peluqueria"))
                .andExpect(content().string(containsString("Peluqueria")))
                .andExpect(content().string(not(containsString("Consulta general"))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - el administrador ve tambien los servicios inactivos")
    void listadoIncluyeInactivos() throws Exception {
        existente("Servicio viejo", EstadoServicio.INACTIVO);

        mockMvc.perform(get("/admin/servicios"))
                .andExpect(content().string(containsString("Servicio viejo")))
                .andExpect(content().string(containsString("INACTIVO")));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al maestro de servicios")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/servicios"))
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/servicios"))
                .andExpect(redirectedUrl("/login"));
    }
}