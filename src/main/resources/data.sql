-- ============================================================
-- Datos semilla de Veterinaria-web
--
-- ON CONFLICT DO NOTHING hace que el script sea idempotente:
-- se puede ejecutar en cada arranque sin duplicar registros.
--
-- Contrasenas (hash BCrypt):
--   admin@veterinaria.com    -> Admin123
--   cliente@correo.com       -> Cliente123
-- CAMBIAR la del administrador antes de publicar la aplicacion.
-- ============================================================

-- ---------- Usuarios ----------
INSERT INTO usuarios (nombre, documento, correo, contrasena, telefono, direccion, ciudad,
                      rol, estado, debe_cambiar_password, intentos_fallidos, fecha_registro)
VALUES (
    'Administrador del Sistema',
    '1000000000',
    'admin@veterinaria.com',
    '$2b$10$l6OT.tjPShLv8wL9fJmtpu4HRMOAKgEbhSIzXnTiywjwg0mjctLbC',
    '3000000000',
    'Calle 1 # 1-01',
    'Medellin',
    'ADMINISTRADOR',
    'ACTIVO',
    false,
    0,
    CURRENT_TIMESTAMP
) ON CONFLICT (correo) DO NOTHING;

INSERT INTO usuarios (nombre, documento, correo, contrasena, telefono, direccion, ciudad,
                      rol, estado, debe_cambiar_password, intentos_fallidos, fecha_registro)
VALUES (
    'Cliente de Prueba',
    '1001001001',
    'cliente@correo.com',
    '$2b$10$VCY7/IquizuUw.vtzM1oO.Ru07PHrKwWiN1yQW/Ke1/vOF.Upu0nW',
    '3101112233',
    'Carrera 50 # 20-30',
    'Medellin',
    'CLIENTE',
    'ACTIVO',
    false,
    0,
    CURRENT_TIMESTAMP
) ON CONFLICT (correo) DO NOTHING;

-- ---------- Catalogo de servicios ----------
-- Duraciones multiplo de 15 minutos, como exige HU-20.
--
-- Se usa WHERE NOT EXISTS en lugar de ON CONFLICT porque la entidad
-- Servicio todavia no tiene restriccion de unicidad sobre "nombre".
-- Cuando se agregue (la exige HU-16), se puede pasar a ON CONFLICT.
INSERT INTO servicios (nombre, descripcion, duracion_minutos, precio_base,
                       es_consulta_veterinaria, estado)
SELECT v.nombre, v.descripcion, v.duracion, v.precio, v.es_consulta, 'ACTIVO'
FROM (VALUES
    ('Consulta general',       'Valoracion medica general de la mascota',       30, 60000.0, true),
    ('Vacunacion',             'Aplicacion de vacunas segun esquema',           15, 45000.0, false),
    ('Desparasitacion',        'Tratamiento antiparasitario interno y externo', 15, 35000.0, false),
    ('Baño y peluqueria',      'Bano medicado, corte y limpieza de oidos',      60, 50000.0, false),
    ('Control post operatorio','Revision de puntos y evolucion',                30, 40000.0, true)
) AS v(nombre, descripcion, duracion, precio, es_consulta)
WHERE NOT EXISTS (
    SELECT 1 FROM servicios s WHERE s.nombre = v.nombre
);