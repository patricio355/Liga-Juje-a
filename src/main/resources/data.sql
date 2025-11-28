-- ================================================
-- 1) ZONAS
-- ================================================
INSERT INTO zonas (id, nombre, descripcion, torneo_id)
VALUES
    (1, 'Zona A', 'Zona principal A', NULL),
    (2, 'Zona B', 'Zona principal B', NULL);


-- ================================================
-- 2) CANCHAS
-- ================================================
INSERT INTO canchas (id, nombre, ubicacion, habilitacion_pdf, valor_entrada, estado, foto_url)
VALUES
    (1, 'Cancha Municipal 1', 'San Salvador de Jujuy', 0, 500, 'HABILITADA', NULL),
    (2, 'Cancha Municipal 2', 'Palpalá', 1, 600, 'HABILITADA', NULL),
    (3, 'Cancha La Tablada', 'Los Perales', 0, 700, 'HABILITADA', NULL);


-- ================================================
-- 3) ENCARGADOS (Usuarios con tipo Encargado)
-- ================================================
INSERT INTO usuarios (id, tipo_usuario, nombre, email, dni, telefono, domicilio, contrasenia)
VALUES
    (1, 'Encargado', 'Ramiro Castaño', 'ramiro@liga.com', '40111222', '3885123456', 'SSJ', '123'),
    (2, 'Administrador', 'Sergio Villalba', 'sergio@liga.com', '38999888', '3885123499', 'Palpalá', '123'),
    (3, 'Arbitro', 'Leonardo Cabrera', 'leo@liga.com', '41222111', '3884121122', 'El Carmen', '123'),
    (4, 'Encargado', 'Diego Benítez', 'diego@liga.com', '37555777', '3884556677', 'San Salvador', '123');


INSERT INTO equipos (id, nombre, localidad, escudo, camiseta_titular, camiseta_suplente, fecha_creacion, estado, cancha_id, zona_id, encargado_id)
VALUES
    (1, 'Atlético Bulón', 'San Salvador', 'https://bulon.com/escudos/atletico.png', 'Rojo', 'Blanco', '2024-10-01', 'ACTIVO', 1, 1, 1),
    (2, 'Los Matadores', 'Palpalá', 'https://bulon.com/escudos/matadores.png', 'Azul', 'Negro', '2024-09-15', 'ACTIVO', 2, 1, 2),
    (3, 'San Martín FC', 'El Carmen', 'https://bulon.com/escudos/sanmartin.png', 'Verde', 'Blanco', '2024-07-20', 'ACTIVO', 3, 2, 3),
    (4, 'Juventud Unida', 'SSJ', 'https://bulon.com/escudos/juventud.png', 'Amarillo', 'Negro', '2024-06-10', 'ACTIVO', 1, 2, 4);