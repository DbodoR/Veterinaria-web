package com.dbodor.veterinariaweb.user;

import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import com.dbodor.veterinariaweb.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HU05CambiarPasswordTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @ParameterizedTest
    @CsvSource({
            "abc12, La contraseña debe tener al menos 8 caracteres",
            "abcdefgh, La contraseña debe contener al menos un número",
            "1234567, La contraseña debe tener al menos 8 caracteres"
    })
    @DisplayName("Criterio 1: Rechazar contraseñas con menos de 8 caracteres o sin números")
    void cambiarPassword_passwordDebil_lanzaIllegalArgumentException(String passwordInvalida, String mensajeEsperado) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cambiarPassword(10L, "Actual123", passwordInvalida);
        });

        assertTrue(ex.getMessage().contains(mensajeEsperado),
                "El mensaje debe explicar el requisito incumplido");
    }

    @Test
    @DisplayName("Criterio 2: Ingresar una contraseña actual incorrecta rechaza el cambio y no guarda")
    void cambiarPassword_passwordActualIncorrecta_lanzaIllegalArgumentException() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setContrasena("$2a$10$hashActualSimulado");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ClaveIncorrecta1", usuario.getContrasena())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cambiarPassword(10L, "ClaveIncorrecta1", "NuevaClaveSegura2026");
        }, "Debe lanzar excepción si la contraseña actual no coincide");

        assertEquals("La contraseña actual es incorrecta", ex.getMessage());
    }

    @Test
    @DisplayName("Criterio 3: Cambiar la contraseña correctamente actualiza el hash, pone debeCambiarPassword en false y persiste")
    void cambiarPassword_datosValidos_actualizaPasswordYPoneFlagEnFalso() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setContrasena("$2a$10$hashAnterior");
        usuario.setDebeCambiarPassword(true);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("PasswordActual123", "$2a$10$hashAnterior")).thenReturn(true);
        when(passwordEncoder.encode("NuevaPassword2026")).thenReturn("$2a$10$nuevoHashEncriptado");

        clienteService.cambiarPassword(10L, "PasswordActual123", "NuevaPassword2026");

        assertEquals("$2a$10$nuevoHashEncriptado", usuario.getContrasena(),
                "La contraseña debe actualizarse con el nuevo valor encriptado");
        assertFalse(usuario.getDebeCambiarPassword(),
                "El indicador debeCambiarPassword debe pasar a falso");
    }

    @Test
    @DisplayName("Criterio 4: Usuario recién creado por administrador tiene debeCambiarPassword en true obligando al cambio")
    void debeCambiarPassword_cuentaNuevaCreadaPorAdmin_retornaTrue() {
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setIdUsuario(25L);
        usuarioNuevo.setCorreo("nuevo.cliente@vetcare.com");
        usuarioNuevo.setContrasena("$2a$10$hashPasswordProvisional");
        usuarioNuevo.setDebeCambiarPassword(Boolean.TRUE);

        when(usuarioRepository.findById(25L)).thenReturn(Optional.of(usuarioNuevo));

        boolean requiereCambio = clienteService.debeCambiarPassword(25L);

        assertTrue(requiereCambio, "Un usuario recién creado debe estar obligado a cambiar su contraseña");
    }

}