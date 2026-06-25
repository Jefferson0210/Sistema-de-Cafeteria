-- V1__init.sql
-- Esquema inicial. Exportado con mysqldump --no-data del esquema que Hibernate
-- (ddl-auto=update, Spring Boot 2.7.18) genera hoy, para que Flyway lo reproduzca idéntico.
-- FOREIGN_KEY_CHECKS=0 porque las tablas se crean en orden alfabético con FKs hacia tablas posteriores.

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accion` varchar(50) NOT NULL,
  `detalle` varchar(500) DEFAULT NULL,
  `entidad` varchar(50) DEFAULT NULL,
  `entidad_id` bigint(20) DEFAULT NULL,
  `fecha` datetime NOT NULL,
  `usuario` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_fecha` (`fecha`),
  KEY `idx_audit_usuario` (`usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `UK_46ccwnsi9409t36lurvtyljak` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalle_pedido` (
  `id_detalle_pedido` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) NOT NULL,
  `notas` varchar(255) DEFAULT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `id_pedido` bigint(20) NOT NULL,
  `id_producto` bigint(20) NOT NULL,
  PRIMARY KEY (`id_detalle_pedido`),
  KEY `DETALLE_PEDIDO_FK` (`id_pedido`),
  KEY `DETALLE_PRODUCTO_FK` (`id_producto`),
  CONSTRAINT `DETALLE_PEDIDO_FK` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE CASCADE,
  CONSTRAINT `DETALLE_PRODUCTO_FK` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `detalles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` double DEFAULT NULL,
  `precio_unitario` decimal(10,2) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL,
  `factura_id_factura` bigint(20) DEFAULT NULL,
  `producto` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj4bouk0xyfw322w8mect3tqo9` (`factura_id_factura`),
  KEY `PRODUCTO_DETALLE_FK` (`producto`),
  CONSTRAINT `FKj4bouk0xyfw322w8mect3tqo9` FOREIGN KEY (`factura_id_factura`) REFERENCES `factura` (`id_factura`) ON DELETE CASCADE,
  CONSTRAINT `PRODUCTO_DETALLE_FK` FOREIGN KEY (`producto`) REFERENCES `producto` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `empresa` (
  `ruc` varchar(13) NOT NULL,
  `activo` bit(1) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_creacion` datetime NOT NULL,
  `iva` decimal(5,2) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `nombre_comercial` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ruc`),
  UNIQUE KEY `UK_2fqlxbcs4h827hio1qam0dhd3` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `factura` (
  `id_factura` bigint(20) NOT NULL AUTO_INCREMENT,
  `descuento` decimal(10,2) DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_emision` datetime NOT NULL,
  `iva` decimal(10,2) NOT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `numero_factura` varchar(50) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `id_cajero` bigint(20) NOT NULL,
  `id_cliente` bigint(20) DEFAULT NULL,
  `ruc_empresa` varchar(13) DEFAULT NULL,
  `id_pedido` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_factura`),
  UNIQUE KEY `UK_q4rschxwntd1d2yt1heie7l5j` (`numero_factura`),
  KEY `idx_factura_numero` (`numero_factura`),
  KEY `idx_factura_fecha` (`fecha_emision`),
  KEY `idx_factura_estado` (`estado`),
  KEY `FACTURA_CAJERO_FK` (`id_cajero`),
  KEY `FACTURA_CLIENTE_FK` (`id_cliente`),
  KEY `FACTURA_EMPRESA_FK` (`ruc_empresa`),
  KEY `FACTURA_PEDIDO_FK` (`id_pedido`),
  CONSTRAINT `FACTURA_CAJERO_FK` FOREIGN KEY (`id_cajero`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `FACTURA_CLIENTE_FK` FOREIGN KEY (`id_cliente`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `FACTURA_EMPRESA_FK` FOREIGN KEY (`ruc_empresa`) REFERENCES `empresa` (`ruc`),
  CONSTRAINT `FACTURA_PEDIDO_FK` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favoritos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `producto` bigint(20) NOT NULL,
  `usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FAVORITO_PRODUCTO_FK` (`producto`),
  KEY `FAVORITO_USUARIO_FK` (`usuario`),
  CONSTRAINT `FAVORITO_PRODUCTO_FK` FOREIGN KEY (`producto`) REFERENCES `producto` (`id_producto`),
  CONSTRAINT `FAVORITO_USUARIO_FK` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `mesa` (
  `id_mesa` bigint(20) NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `capacidad` int(11) NOT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_creacion` datetime NOT NULL,
  `numero_mesa` int(11) NOT NULL,
  `ubicacion` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_mesa`),
  UNIQUE KEY `UK_9cvc8klh8kwomeofl0i7sa5g0` (`numero_mesa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pago` (
  `id_pago` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_pago` datetime NOT NULL,
  `metodo_pago` varchar(20) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  `id_factura` bigint(20) NOT NULL,
  PRIMARY KEY (`id_pago`),
  KEY `idx_pago_fecha` (`fecha_pago`),
  KEY `idx_pago_metodo` (`metodo_pago`),
  KEY `PAGO_FACTURA_FK` (`id_factura`),
  CONSTRAINT `PAGO_FACTURA_FK` FOREIGN KEY (`id_factura`) REFERENCES `factura` (`id_factura`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pedido` (
  `id_pedido` bigint(20) NOT NULL AUTO_INCREMENT,
  `estado` varchar(20) NOT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_pedido` datetime NOT NULL,
  `iva` decimal(10,2) DEFAULT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL,
  `total` decimal(10,2) DEFAULT NULL,
  `id_cliente` bigint(20) DEFAULT NULL,
  `id_mesa` bigint(20) DEFAULT NULL,
  `id_mesero` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_pedido`),
  KEY `idx_pedido_estado` (`estado`),
  KEY `idx_pedido_fecha` (`fecha_pedido`),
  KEY `PEDIDO_CLIENTE_FK` (`id_cliente`),
  KEY `PEDIDO_MESA_FK` (`id_mesa`),
  KEY `PEDIDO_MESERO_FK` (`id_mesero`),
  CONSTRAINT `PEDIDO_CLIENTE_FK` FOREIGN KEY (`id_cliente`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `PEDIDO_MESA_FK` FOREIGN KEY (`id_mesa`) REFERENCES `mesa` (`id_mesa`),
  CONSTRAINT `PEDIDO_MESERO_FK` FOREIGN KEY (`id_mesero`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `producto` (
  `id_producto` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` text DEFAULT NULL,
  `disponible` bit(1) NOT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_creacion` datetime NOT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL,
  `version` bigint(20) NOT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_producto`),
  KEY `idx_producto_nombre` (`nombre`),
  KEY `idx_producto_disponible` (`disponible`),
  KEY `PRODUCTO_CATEGORY_FK` (`category_id`),
  CONSTRAINT `PRODUCTO_CATEGORY_FK` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reserva` (
  `id_reserva` bigint(20) NOT NULL AUTO_INCREMENT,
  `duracion_horas` int(11) NOT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_creacion` datetime NOT NULL,
  `fecha_reserva` datetime NOT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `num_personas` int(11) NOT NULL,
  `id_mesa` bigint(20) NOT NULL,
  `id_usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id_reserva`),
  KEY `idx_reserva_fecha` (`fecha_reserva`),
  KEY `idx_reserva_estado` (`estado`),
  KEY `RESERVA_MESA_FK` (`id_mesa`),
  KEY `RESERVA_USUARIO_FK` (`id_usuario`),
  CONSTRAINT `RESERVA_MESA_FK` FOREIGN KEY (`id_mesa`) REFERENCES `mesa` (`id_mesa`) ON DELETE CASCADE,
  CONSTRAINT `RESERVA_USUARIO_FK` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rol` varchar(255) NOT NULL,
  `usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ROL_USUARIO_UK` (`rol`,`usuario`),
  KEY `ROL_USUARIO_FK` (`usuario`),
  CONSTRAINT `ROL_USUARIO_FK` FOREIGN KEY (`usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `usuario` (
  `id_usuario` bigint(20) NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `fecha_actualizacion` datetime NOT NULL,
  `fecha_registro` datetime NOT NULL,
  `foto_url` varchar(500) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UK_5171l57faosmj8myawaucatdw` (`email`),
  UNIQUE KEY `UK_863n1y3x0jalatoir4325ehal` (`username`),
  KEY `idx_usuario_username` (`username`),
  KEY `idx_usuario_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
