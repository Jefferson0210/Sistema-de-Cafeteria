package tics.uide.gestionuide.enums;

/**
 * Modo de cuenta de una mesa durante una sesión (visita).
 * COMUN: todos los comensales suman al mismo pedido (una sola cuenta).
 * SEPARADA: cada comensal tiene su propio pedido (varias cuentas).
 * Se fija en el primer escaneo y NO cambia mientras la sesión está abierta.
 */
public enum ModoCuenta {
    COMUN,
    SEPARADA
}
