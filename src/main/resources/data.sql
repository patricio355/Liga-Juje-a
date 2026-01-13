
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
INSERT INTO usuarios
(id, nombre, email, dni, telefono, domicilio, contrasenia, rol, activo)
VALUES
    (1,  'Patricio Quiroga', 'patricioquiroga355@gmail.com',
     '41844570', '3885123456', 'SSJ',
     '$2a$10$iYVMDyD5MgsydByu2jN37.DPv/hd/f9cpt/oP.zQBnkQNlyMoWs4G',
     'ADMIN', true);