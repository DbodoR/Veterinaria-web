package com.dbodor.veterinariaweb.config;

import com.dbodor.veterinariaweb.model.EstadoUsuario;
import com.dbodor.veterinariaweb.model.RolUsuario;
import com.dbodor.veterinariaweb.model.Usuario;
import com.dbodor.veterinariaweb.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/* 
  Crea la cuenta de administrador en el servidor publico.
 
  En produccion el data.sql no se ejecuta (spring.sql.init.mode=never), asi que
  sin esto la aplicacion desplegada arrancaria sin ningun usuario y nadie podria
  entrar.
 
  La contrasena llega por variable de entorno, nunca escrita en el repositorio:
 
    ADMIN_CORREO     correo de la cuenta (opcional, por defecto admin@veterinaria.com)
    ADMIN_PASSWORD   contrasena en texto plano; se guarda cifrada con BCrypt
    ADMIN_DOCUMENTO  documento de la cuenta (opcional)
 
  Si ya existe un administrador, no hace nada: es seguro reiniciar la aplicacion
  las veces que haga falta.
 */
@Component
@Profile("prod")
public class AdminInicialSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_CORREO:admin@veterinaria.com}")
    private String correo;

    @Value("${ADMIN_PASSWORD:}")
    private String password;

    @Value("${ADMIN_DOCUMENTO:1000000000}")
    private String documento;

    public AdminInicialSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.countByRolAndEstado(RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO) > 0) {
            log.info("Ya existe un administrador activo; no se crea ninguno.");
            return;
        }

        if (password == null || password.isBlank()) {
            log.error("No hay administrador y la variable ADMIN_PASSWORD esta vacia. "
                    + "Definila en el panel del servidor y reinicia la aplicacion.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador del Sistema");
        admin.setDocumento(documento);
        admin.setCorreo(correo);
        admin.setContrasena(passwordEncoder.encode(password));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setDebeCambiarPassword(Boolean.FALSE);

        usuarioRepository.save(admin);
        log.info("Administrador inicial creado con el correo {}", correo);
    }
}