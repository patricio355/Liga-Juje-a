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
    (1, 'Cancha Municipal 1', 'San Salvador de Jujuy', 0, 500, true, NULL),
    (2, 'Cancha Municipal 2', 'Palpalá', 1, 600, true, NULL),
    (3, 'Cancha La Tablada', 'Los Perales', 0, 700, true, NULL);


-- ================================================
-- 3) ENCARGADOS (Usuarios con tipo Encargado)
-- ================================================
INSERT INTO usuarios (id, tipo_usuario, nombre, email, dni, telefono, domicilio, contrasenia , rol)
VALUES
    (1, 'Encargado', 'Patricio Quiroga', 'patricioquiroga355@gmail.com', '41844570', '3885123456', 'SSJ', '$2a$10$iYVMDyD5MgsydByu2jN37.DPv/hd/f9cpt/oP.zQBnkQNlyMoWs4G','ADMIN'),
    (2, 'Encargado', 'Sergio Villalba', 'sergio@liga.com', '38999888', '3885123499', 'Palpalá', '123','ADMIN'),
    (3, 'Encargado', 'Leonardo Cabrera', 'leo@liga.com', '41222111', '3884121122', 'El Carmen', '123','ADMIN'),
    (4, 'Encargado', 'Diego Benítez', 'diego@liga.com', '37555777', '3884556677', 'San Salvador', '$2a$10$iYVMDyD5MgsydByu2jN37.DPv/hd/f9cpt/oP.zQBnkQNlyMoWs4G','ENCARGADO');


INSERT INTO equipos (id, nombre, localidad, escudo, camiseta_titular, camiseta_suplente, fecha_creacion, estado, cancha_id, encargado_id)
VALUES
    (1, 'Atlético Bulón', 'San Salvador', 'https://bulon.com/escudos/atletico.png', 'Rojo', 'Blanco', '2024-10-01', true, 1,  1),
    (2, 'Los Matadores', 'Palpalá', 'https://bulon.com/escudos/matadores.png', 'Azul', 'Negro', '2024-09-15', true, 2,  2),
    (3, 'San Martín FC', 'El Carmen', 'https://bulon.com/escudos/sanmartin.png', 'Verde', 'Blanco', '2024-07-20', true, 3,  3),
    (4, 'Juventud Unida', 'SSJ', 'https://bulon.com/escudos/juventud.png', 'Amarillo', 'Negro', '2024-06-10', true, 1,  4);