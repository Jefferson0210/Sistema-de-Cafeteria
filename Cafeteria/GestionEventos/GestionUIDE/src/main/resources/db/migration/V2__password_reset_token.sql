-- V2__password_reset_token.sql
-- Tabla de tokens de recuperación de contraseña (un solo uso, expira en 1 hora).
-- Exportado con mysqldump --no-data del esquema que genera Hibernate (igual que V1),
-- para que ddl-auto=validate cuadre exacto. `usuario` ya existe (creada en V1).

CREATE TABLE `password_reset_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expira_en` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `usado` bit(1) NOT NULL,
  `id_usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_anlf8vm14i1nn7pa6qlp0xa3w` (`token_hash`),
  KEY `PWRESET_USUARIO_FK` (`id_usuario`),
  CONSTRAINT `PWRESET_USUARIO_FK` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
