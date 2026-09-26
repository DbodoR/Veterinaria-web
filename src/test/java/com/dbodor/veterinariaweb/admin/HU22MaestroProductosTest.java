package com.dbodor.veterinariaweb.admin;

import com.dbodor.veterinariaweb.enums.EstadoProducto;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.repository.ProductoRepository;
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
 * HU-22 Catalogo de medicamentos: pantallas del maestro de productos.
 *
 * Las reglas de negocio ya estan cubiertas por ProductoServiceTest con
 * pruebas unitarias. Estas pruebas verifican los mismos criterios de
 * punta a punta, desde el formulario hasta la base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU22MaestroProductosTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ProductoRepository productoRepository;

    @BeforeEach
    @AfterEach
    void limpiar() {
        productoRepository.deleteAll();
    }

    // ---------- Datos de apoyo ----------

    private Producto producto(String nombre, String presentacion, EstadoProducto estado) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPresentacion(presentacion);
        p.setPrecioUnitario(18000.0);
        p.setStock(20);
        p.setEsRecetableVeterinario(true);
        p.setEstado(estado);
        return productoRepository.save(p);
    }

    private ResultActions registrar(String nombre, String presentacion, String precio, String stock) throws Exception {
        return mockMvc.perform(post("/admin/productos").with(csrf())
                .param("nombre", nombre)
                .param("presentacion", presentacion)
                .param("precioUnitario", precio)
                .param("stock", stock)
                .param("esRecetableVeterinario", "true"));
    }

    // ---------- CA1: el producto queda registrado y activo ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - un producto valido se guarda y redirige al listado")
    void productoValidoSeGuarda() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "18000", "20")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos"));

        assertThat(productoRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - el producto queda ACTIVO y con todos sus datos")
    void productoQuedaActivoConSusDatos() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "18000", "20");

        Producto guardado = productoRepository.findAll().getFirst();
        assertThat(guardado.getNombre()).isEqualTo("Amoxicilina");
        assertThat(guardado.getPresentacion()).isEqualTo("Tabletas 250 mg");
        assertThat(guardado.getPrecioUnitario()).isEqualTo(18000.0);
        assertThat(guardado.getStock()).isEqualTo(20);
        assertThat(guardado.getEsRecetableVeterinario()).isTrue();
        assertThat(guardado.getEstado()).isEqualTo(EstadoProducto.ACTIVO);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - un campo obligatorio vacio impide guardar y no persiste nada")
    void nombreVacioSeRechaza() throws Exception {
        registrar("", "Tabletas 250 mg", "18000", "20")
                .andExpect(status().isOk())
                .andExpect(view().name("admin/productos/formulario"))
                .andExpect(model().attributeHasFieldErrors("productoForm", "nombre"));

        assertThat(productoRepository.count()).isZero();
    }

    // ---------- CA2: precio positivo y stock no negativo ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un precio igual a cero se rechaza")
    void precioCeroSeRechaza() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "0", "20")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productoForm", "precioUnitario"));

        assertThat(productoRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un precio negativo se rechaza")
    void precioNegativoSeRechaza() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "-500", "20")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productoForm", "precioUnitario"));

        assertThat(productoRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un stock negativo se rechaza")
    void stockNegativoSeRechaza() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "18000", "-1")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productoForm", "stock"));

        assertThat(productoRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un stock igual a cero se acepta")
    void stockCeroSeAcepta() throws Exception {
        registrar("Amoxicilina", "Tabletas 250 mg", "18000", "0")
                .andExpect(status().is3xxRedirection());

        assertThat(productoRepository.count()).isEqualTo(1);
    }

    // ---------- CA3: desactivar sin borrar ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - desactivar deja el producto INACTIVO sin eliminarlo")
    void desactivarDejaInactivo() throws Exception {
        Producto p = producto("Amoxicilina", "Tabletas 250 mg", EstadoProducto.ACTIVO);

        mockMvc.perform(post("/admin/productos/" + p.getIdProducto() + "/desactivar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos"));

        assertThat(productoRepository.count()).isEqualTo(1);
        assertThat(productoRepository.findById(p.getIdProducto()).orElseThrow().getEstado())
                .isEqualTo(EstadoProducto.INACTIVO);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - el producto desactivado sigue apareciendo en el listado como INACTIVO")
    void desactivadoSigueEnListado() throws Exception {
        producto("Meloxicam", "Suspension oral 15 ml", EstadoProducto.INACTIVO);

        mockMvc.perform(get("/admin/productos"))
                .andExpect(content().string(containsString("Meloxicam")))
                .andExpect(content().string(containsString("INACTIVO")));
    }

    // ---------- Listado ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - muestra los productos con nombre, presentacion, stock y estado")
    void listadoMuestraSusDatos() throws Exception {
        producto("Amoxicilina", "Tabletas 250 mg", EstadoProducto.ACTIVO);

        mockMvc.perform(get("/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/productos/lista"))
                .andExpect(model().attribute("productos", hasSize(1)))
                .andExpect(content().string(containsString("Amoxicilina")))
                .andExpect(content().string(containsString("Tabletas 250 mg")))
                .andExpect(content().string(containsString("ACTIVO")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Listado - la busqueda filtra por nombre")
    void busquedaPorNombre() throws Exception {
        producto("Amoxicilina", "Tabletas 250 mg", EstadoProducto.ACTIVO);
        producto("Meloxicam", "Suspension oral 15 ml", EstadoProducto.ACTIVO);

        mockMvc.perform(get("/admin/productos").param("buscar", "amox"))
                .andExpect(model().attribute("productos", hasSize(1)))
                .andExpect(model().attribute("productos", hasItem(hasProperty("nombre", is("Amoxicilina")))));
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al maestro de productos")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/productos"))
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/productos"))
                .andExpect(redirectedUrl("/login"));
    }
}