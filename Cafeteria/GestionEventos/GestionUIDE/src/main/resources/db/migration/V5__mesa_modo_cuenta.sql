-- V5__mesa_modo_cuenta.sql
-- Modo de cuenta de la sesión de mesa (COMUN | SEPARADA), null = mesa sin sesión.
-- Tipo tomado del volcado de Hibernate (SHOW CREATE TABLE mesa) para que ddl-auto=validate cuadre.

ALTER TABLE `mesa` ADD COLUMN `modo_cuenta` varchar(20) DEFAULT NULL;
