# VetCare Plus - Sistema de Gestión Veterinaria Web

Plataforma web integral para la administración clínica veterinaria, gestión de historias clínicas, agendamiento de citas y portal de autoservicio para clientes.

Enlace de la web: https://veterinaria-web-i88k.onrender.com

## Documentación

Los documentos detallados del proyecto se encuentran disponibles en la carpeta `docs/`:

* [Manual de Instalación de la aplicación](docs/Manual-Instalacion-Veterinaria-Web.pdf)
* [Historias de usuario](docs/Historias-Usuario-Veterinaria-Web.pdf)
* [Manual de Usuario](docs/Manual_de_usuario.pdf)
* [Informe Pruebas TDD](docs/Informe_de_Pruebas_TDD_-_Sistema_Veterinario.pdf)

---

## 1. Descripción General

VetCare Plus es una solución desarrollada con Spring Boot, Spring Security, JPA/Hibernate y Thymeleaf orientada a centralizar y automatizar los procesos operativos de una clínica veterinaria. El sistema implementa control de acceso basado en roles (RBAC) con interfaces especializadas para administradores, personal veterinario y clientes finales.

---

## 2. Roles y Módulos del Sistema

### Administrador
* Gestión de maestros: registro, consulta y actualización de Clientes, Veterinarios, Mascotas, Servicios y Medicamentos/Productos.
* Panel de control con métricas en tiempo real sobre citas del día, citas pendientes y clientes activos.
* Gestión transaccional de agendamiento de citas con validación de franjas horarias y disponibilidad de profesionales.
* Módulo de reportes financieros y de historias clínicas.

### Veterinario
* Consulta de agenda médica diaria y semanal.
* Registro de atención, evolución clínica y diagnósticos en Historias Clínicas.
* Prescripción de recetas médicas con cálculo de stock y vinculación al catálogo de medicamentos.

### Cliente
* Portal de autoservicio con visualización de estado de cuenta y citas programadas.
* Gestión de perfil de usuario (actualización de datos de contacto y cambio de contraseña).
* Registro y visualización del listado de mascotas propias con cálculo automático de edad.
* Agendamiento de citas médicas seleccionando mascota, servicio, veterinario disponible y franjas horarias en múltiplos de 15 minutos.

---

## 3. Seguridad y Políticas de Acceso

* Autenticación y Autorización: Implementado mediante Spring Security con aislamiento estricto de rutas (`/admin/**`, `/veterinario/**`, `/cliente/**`).
* Cifrado de Contraseñas: Algoritmo BCrypt aplicado en todas las credenciales de acceso[cite: 1].
* Control de Intentos Fallidos: Bloqueo temporal de la cuenta por 15 minutos tras 5 intentos fallidos consecutivos[cite: 1].
* Política de Primer Acceso: Generación de contraseñas temporales al dar de alta usuarios y redirección obligatoria a `/cambiar-password` antes de permitir navegación en el sistema[cite: 1].
* Auditoría de Entidades: Registro automático de fechas de creación y modificación mediante anotaciones `@PrePersist` y `@PreUpdate`[cite: 1].

---

## 4. Stack Tecnológico

* Lenguaje: Java 17+ / Java 21.
* Framework Principal: Spring Boot 4.
* Seguridad: Spring Security 7.
* Persistencia de Datos: Spring Data JPA, Hibernate, PostgreSQL / H2 Database.
* Motor de Plantillas: Thymeleaf con integración de Spring Security Extras.
* Diseño Frontend: Tailwind CSS, FontAwesome, Google Material Symbols.
* Pruebas Automatizadas: JUnit 5, Mockito, MockMvc, AssertJ.
* Herramienta de Construcción: Apache Maven.
* Despliegue: Render.

---

## 5. Arquitectura del Proyecto

```text
src/main/java/com/dbodor/veterinariaweb/
├── config/        # Configuraciones de seguridad y seeders de inicio
├── controller/    # Controladores MVC para Admin, Cliente y Auth
├── dto/           # Data Transfer Objects y formularios validados
├── enums/         # Enumeraciones de dominio (RolUsuario, EstadoUsuario, etc.)
├── model/         # Entidades del modelo relacional JPA
├── repository/    # Interfaces de acceso a datos con consultas JPQL
├── security/      # Handlers de éxito/fallo, UserDetails y filtros
└── service/       # Lógica de negocio (interfaces e implementaciones)
