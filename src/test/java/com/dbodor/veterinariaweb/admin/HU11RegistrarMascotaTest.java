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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-11 Registrar una mascota.
 *
 * Alta desde el maestro de mascotas del panel de administracion. Cada
 * criterio de aceptacion se traduce en uno o mas metodos de prueba.
 *
 * Citas y mascotas tienen clave foranea hacia usuarios, asi que se eliminan
 * antes que ellos para no contaminar las demas clases de prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HU11RegistrarMascotaTest {

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

    private Usuario cliente;

    @BeforeEach
    void preparar() {
        limpiar();
        cliente = usuario("Ana Torres", "1010101010", "ana@correo.com", RolUsuario.CLIENTE, EstadoUsuario.ACTIVO);
    }

    @AfterEach
    void limpiar() {
        citaRepository.deleteAll();
        mascotaRepository.deleteAll();
        veterinarioRepository.deleteAll();
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

    /** Envia el formulario de alta con raza fija y los demas datos indicados. */
    private ResultActions registrar(Long idCliente, String nombre, String especie, String sexo,
                                    String fechaNacimiento, String peso) throws Exception {
        return mockMvc.perform(post("/admin/mascotas").with(csrf())
                .param("idCliente", idCliente == null ? "" : idCliente.toString())
                .param("nombre", nombre)
                .param("especie", especie)
                .param("raza", "Criollo")
                .param("sexo", sexo)
                .param("fechaNacimiento", fechaNacimiento)
                .param("pesoKg", peso));
    }

    private ResultActions registrarValida(String nombre) throws Exception {
        return registrar(cliente.getIdUsuario(), nombre, "Perro", "M", "2022-03-15", "12.5");
    }

    // ---------- CA1: la mascota queda registrada y asociada a su dueno ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - una mascota valida se guarda y redirige al listado")
    void mascotaValidaSeGuarda() throws Exception {
        registrarValida("Firulais")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/mascotas"));

        assertThat(mascotaRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - la mascota queda asociada al cliente, activa y con todos sus datos")
    void mascotaQuedaAsociadaYActiva() throws Exception {
        registrarValida("Firulais");

        assertThat(mascotaRepository.porDuenoYEstado(cliente.getIdUsuario(), "ACTIVO")).hasSize(1);

        Mascota guardada = mascotaRepository.findAll().getFirst();
        assertThat(guardada.getNombre()).isEqualTo("Firulais");
        assertThat(guardada.getEspecie()).isEqualTo("Perro");
        assertThat(guardada.getRaza()).isEqualTo("Criollo");
        assertThat(guardada.getSexo()).isEqualTo("M");
        assertThat(guardada.getFechaNacimiento()).isEqualTo(LocalDate.of(2022, 3, 15));
        assertThat(guardada.getPesoKg()).isEqualTo(12.5);
        assertThat(guardada.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA1 - un campo obligatorio vacio impide guardar y no persiste nada")
    void nombreVacioSeRechaza() throws Exception {
        registrar(cliente.getIdUsuario(), "", "Perro", "M", "2022-03-15", "12.5")
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mascotas/formulario"))
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "nombre"));

        assertThat(mascotaRepository.count()).isZero();
    }

    // ---------- CA2: el dueno debe ser un cliente activo ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - el formulario ofrece como dueno solo clientes activos")
    void soloClientesActivosComoDueno() throws Exception {
        usuario("Luis Diaz", "2020202020", "luis@correo.com", RolUsuario.CLIENTE, EstadoUsuario.INACTIVO);
        usuario("Laura Gomez", "3030303030", "laura@veterinaria.com", RolUsuario.VETERINARIO, EstadoUsuario.ACTIVO);

        mockMvc.perform(get("/admin/mascotas/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mascotas/formulario"))
                .andExpect(model().attribute("clientes", hasSize(1)))
                .andExpect(model().attribute("clientes", hasItem(hasProperty("nombre", is("Ana Torres")))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - un dueno que no es cliente se rechaza aunque se envie a mano")
    void duenoQueNoEsClienteSeRechaza() throws Exception {
        Usuario veterinaria = usuario("Laura Gomez", "3030303030", "laura@veterinaria.com",
                RolUsuario.VETERINARIO, EstadoUsuario.ACTIVO);

        registrar(veterinaria.getIdUsuario(), "Firulais", "Perro", "M", "2022-03-15", "12.5")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "idCliente"));

        assertThat(mascotaRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA2 - sin dueno no se puede registrar")
    void sinDuenoSeRechaza() throws Exception {
        registrar(null, "Firulais", "Perro", "M", "2022-03-15", "12.5")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "idCliente"));

        assertThat(mascotaRepository.count()).isZero();
    }

    // ---------- CA3: la especie se elige de una lista cerrada ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - el formulario ofrece solo perro, gato, ave, roedor u otro")
    void especiesOfrecidas() throws Exception {
        mockMvc.perform(get("/admin/mascotas/nueva"))
                .andExpect(model().attribute("especies", is(List.of("Perro", "Gato", "Ave", "Roedor", "Otro"))));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA3 - una especie fuera de la lista se rechaza")
    void especieFueraDeListaSeRechaza() throws Exception {
        registrar(cliente.getIdUsuario(), "Manuelita", "Tortuga", "H", "2022-03-15", "1.2")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "especie"));

        assertThat(mascotaRepository.count()).isZero();
    }

    // ---------- CA4: la fecha de nacimiento no puede ser futura ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - una fecha de nacimiento futura se rechaza")
    void fechaFuturaSeRechaza() throws Exception {
        registrar(cliente.getIdUsuario(), "Firulais", "Perro", "M",
                LocalDate.now().plusDays(1).toString(), "12.5")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "fechaNacimiento"));

        assertThat(mascotaRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA4 - una mascota nacida hoy se acepta")
    void fechaDeHoySeAcepta() throws Exception {
        registrar(cliente.getIdUsuario(), "Cachorro", "Perro", "M", LocalDate.now().toString(), "0.4")
                .andExpect(status().is3xxRedirection());

        assertThat(mascotaRepository.count()).isEqualTo(1);
    }

    // ---------- CA5: el peso debe ser mayor que cero ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - un peso igual a cero se rechaza")
    void pesoCeroSeRechaza() throws Exception {
        registrar(cliente.getIdUsuario(), "Firulais", "Perro", "M", "2022-03-15", "0")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "pesoKg"));

        assertThat(mascotaRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("CA5 - un peso negativo se rechaza")
    void pesoNegativoSeRechaza() throws Exception {
        registrar(cliente.getIdUsuario(), "Firulais", "Perro", "M", "2022-03-15", "-3")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "pesoKg"));

        assertThat(mascotaRepository.count()).isZero();
    }

    // ---------- Regla del modelo: nombre unico por dueno ----------

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Regla - un mismo dueno no puede tener dos mascotas con el mismo nombre")
    void nombreRepetidoParaElMismoDuenoSeRechaza() throws Exception {
        registrarValida("Firulais");

        registrarValida("firulais")
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mascotaForm", "nombre"));

        assertThat(mascotaRepository.count()).isEqualTo(1);
    }

    // ---------- Control de acceso ----------

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Acceso - un cliente no entra al alta de mascotas del administrador")
    void clienteNoEntra() throws Exception {
        mockMvc.perform(get("/admin/mascotas/nueva"))
                .andExpect(redirectedUrl("/cliente/citas"));
    }

    @Test
    @DisplayName("Acceso - sin sesion redirige al login")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/admin/mascotas/nueva"))
                .andExpect(redirectedUrl("/login"));
    }
}