-- V4__refresh_token.sql
-- Refresh tokens (opacos, hash SHA-256, revocables, rotados). Mismo patrón que los otros tokens.
-- Tipos tomados del esquema que genera Hibernate (igual que V1..V3) para que ddl-auto=validate cuadre.

CREATE TABLE `refresh_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expira_en` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `revocado` bit(1) NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `id_usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kdj16cltjxdksuyiosdhliveg` (`token_hash`),
  KEY `REFRESH_USUARIO_FK` (`id_usuario`),
  CONSTRAINT `REFRESH_USUARIO_FK` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
