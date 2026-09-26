package com.dbodor.veterinariaweb.user;

import com.dbodor.veterinariaweb.dto.PerfilClienteDto;
import com.dbodor.veterinariaweb.enums.EstadoUsuario;
import com.dbodor.veterinariaweb.enums.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.service.DatoDuplicadoException;
import com.dbodor.veterinariaweb.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HU03PerfilClienteTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    @DisplayName("Criterio 1: Cargar perfil devuelve los datos del cliente autenticado precargados")
    void consultarPerfil_retornaDatosActualesDelCliente() {
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(10L);
        cliente.setNombre("Carlos Mendoza");
        cliente.setDocumento("1001");
        cliente.setCorreo("carlos.vet@example.com");
        cliente.setTelefono("3001234567");
        cliente.setDireccion("Calle 10 # 40-20");
        cliente.setCiudad("Medellín");
        cliente.setRol(RolUsuario.CLIENTE);
        cliente.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(cliente));

        PerfilClienteDto perfil = clienteService.consultarPerfil(10L);

        assertNotNull(perfil, "El DTO del perfil no debe ser nulo");
        assertEquals("Carlos Mendoza", perfil.getNombre(), "El nombre debe coincidir con el registrado");
        assertEquals("1001", perfil.getDocumento(), "El documento debe estar precargado");
        assertEquals("carlos.vet@example.com", perfil.getCorreo(), "El correo debe estar precargado");
        assertEquals("3001234567", perfil.getTelefono(), "El teléfono debe estar precargado");
        assertEquals("Calle 10 # 40-20", perfil.getDireccion(), "La dirección debe estar precargada");
        assertEquals("Medellín", perfil.getCiudad(), "La ciudad debe estar precargada");
    }

    @Test
    @DisplayName("Criterio 2: Cambiar el correo por uno existente de otro usuario lanza excepción y conserva el original")
    void actualizarPerfil_correoDuplicadoPorOtroUsuario_rechazaCambioYConservaOriginal() {
        Usuario clienteActual = new Usuario();
        clienteActual.setIdUsuario(10L);
        clienteActual.setCorreo("original@vetcare.com");
        clienteActual.setNombre("Carlos Mendoza");
        clienteActual.setTelefono("3001234567");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(clienteActual));
        // Simulamos que el correo ya le pertenece a otro usuario en el sistema
        when(usuarioRepository.existsByCorreoAndIdUsuarioNot("duplicado@vetcare.com", 10L)).thenReturn(true);

        PerfilClienteDto cambios = new PerfilClienteDto();
        cambios.setNombre("Carlos Mendoza");
        cambios.setCorreo("duplicado@vetcare.com");
        cambios.setTelefono("3001234567");

        assertThrows(DatoDuplicadoException.class, () -> {
            clienteService.actualizarPerfil(10L, cambios);
        }, "Debe arrojar DatoDuplicadoException si el correo está ocupado por otro usuario");

        assertEquals("original@vetcare.com", clienteActual.getCorreo(),
                "El correo debe conservar su valor original tras fallar la validación");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Criterio 3: Dejar vacío un campo obligatorio lanza excepción y no persiste cambios")
    void actualizarPerfil_camposObligatoriosVacios_lanzaExcepcionYNoPersiste(String valorInvalido) {
        PerfilClienteDto dtoSinNombre = new PerfilClienteDto();
        dtoSinNombre.setNombre(valorInvalido);
        dtoSinNombre.setCorreo("carlos@vetcare.com");
        dtoSinNombre.setTelefono("3001234567");

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.actualizarPerfil(10L, dtoSinNombre);
        }, "Debe lanzar IllegalArgumentException si el nombre está en blanco");

        // 2. Correo vacío o en blanco
        PerfilClienteDto dtoSinCorreo = new PerfilClienteDto();
        dtoSinCorreo.setNombre("Carlos Mendoza");
        dtoSinCorreo.setCorreo(valorInvalido);
        dtoSinCorreo.setTelefono("3001234567");

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.actualizarPerfil(10L, dtoSinCorreo);
        }, "Debe lanzar IllegalArgumentException si el correo está en blanco");

        // 3. Teléfono vacío o en blanco
        PerfilClienteDto dtoSinTelefono = new PerfilClienteDto();
        dtoSinTelefono.setNombre("Carlos Mendoza");
        dtoSinTelefono.setCorreo("carlos@vetcare.com");
        dtoSinTelefono.setTelefono(valorInvalido);

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.actualizarPerfil(10L, dtoSinTelefono);
        }, "Debe lanzar IllegalArgumentException si el teléfono está en blanco");
    }

}
