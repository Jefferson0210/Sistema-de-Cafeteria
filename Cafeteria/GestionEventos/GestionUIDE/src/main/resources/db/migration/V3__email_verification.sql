-- V3__email_verification.sql
-- Doble opt-in (verificación de correo):
--   1) columna usuario.email_verificado
--   2) tabla email_verification_token (mismo patrón que password_reset_token)
-- Tipos tomados del esquema que genera Hibernate (igual que V1/V2) para que ddl-auto=validate cuadre.

-- Columna nueva. DEFAULT b'1' + UPDATE marcan como VERIFICADOS a los usuarios YA existentes
-- (retrocompatibilidad: no se bloquea el login de nadie que ya tenía cuenta).
-- Los usuarios nuevos los inserta Hibernate con 0 (no verificado) hasta que confirmen su correo.
ALTER TABLE `usuario` ADD COLUMN `email_verificado` bit(1) NOT NULL DEFAULT b'1';
UPDATE `usuario` SET `email_verificado` = b'1';

CREATE TABLE `email_verification_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expira_en` datetime(6) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `usado` bit(1) NOT NULL,
  `id_usuario` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7ay5cmnpq0hps453t4wg1weqb` (`token_hash`),
  KEY `EMAILVERIF_USUARIO_FK` (`id_usuario`),
  CONSTRAINT `EMAILVERIF_USUARIO_FK` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
