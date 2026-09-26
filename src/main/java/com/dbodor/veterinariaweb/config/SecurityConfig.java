package com.dbodor.veterinariaweb.config;

import com.dbodor.veterinariaweb.security.AccesoDenegadoHandler;
import com.dbodor.veterinariaweb.security.ExitoAutenticacionHandler;
import com.dbodor.veterinariaweb.security.FalloAutenticacionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ExitoAutenticacionHandler exitoHandler;
    private final FalloAutenticacionHandler falloHandler;
    private final AccesoDenegadoHandler accesoDenegadoHandler;

    public SecurityConfig(ExitoAutenticacionHandler exitoHandler,
                          FalloAutenticacionHandler falloHandler,
                          AccesoDenegadoHandler accesoDenegadoHandler) {
        this.exitoHandler = exitoHandler;
        this.falloHandler = falloHandler;
        this.accesoDenegadoHandler = accesoDenegadoHandler;
    }

    /**
     * BCrypt. Las contrasenas nunca se guardan en texto plano; los hashes
     * de data.sql fueron generados con este mismo algoritmo.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos publicos
                .requestMatchers("/","/home", "/login", "/css/**", "/js/**", "/img/**", "/webjars/**")
                    .permitAll()
                // Pantalla de cambio de clave forzado (requiere sesión activa)
                .requestMatchers("/cambiar-password").authenticated()
                // Cada zona exige su rol (HU-21 CA4)
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/veterinario/**").hasRole("VETERINARIO")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                // Cualquier otra pantalla interna exige sesion (HU-02 CA5)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                // El formulario envia el correo en el campo "username"
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(exitoHandler)
                .failureHandler(falloHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accesoDenegadoHandler)
            );

        return http.build();
    }
}