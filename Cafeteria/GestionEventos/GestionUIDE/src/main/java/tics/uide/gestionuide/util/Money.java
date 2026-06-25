package tics.uide.gestionuide.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Punto único de redondeo de dinero: escala 2, HALF_UP.
 * TODA asignación a un campo monetario debe pasar por aquí para mantener
 * la escala consistente y evitar errores de coma flotante.
 */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode MODE = RoundingMode.HALF_UP;

    private Money() {}

    /** Normaliza a escala 2 (null-safe: devuelve null si entra null). */
    public static BigDecimal scale(BigDecimal v) {
        return v == null ? null : v.setScale(SCALE, MODE);
    }

    /** null -> 0.00; si no, escala 2. */
    public static BigDecimal nz(BigDecimal v) {
        return v == null ? zero() : scale(v);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, MODE);
    }

    /** Conversión desde double SOLO para fronteras (DTOs/legacy). Evitar en cálculos. */
    public static BigDecimal of(double v) {
        return scale(BigDecimal.valueOf(v));
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return scale(nz(a).add(nz(b)));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return scale(nz(a).subtract(nz(b)));
    }

    public static BigDecimal multiply(BigDecimal a, long cantidad) {
        return scale(nz(a).multiply(BigDecimal.valueOf(cantidad)));
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return scale(nz(a).multiply(nz(b)));
    }

    public static BigDecimal sum(Collection<BigDecimal> valores) {
        return scale(valores.stream().map(Money::nz).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static boolean gt(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) > 0;
    }

    public static boolean gte(BigDecimal a, BigDecimal b) {
        return nz(a).compareTo(nz(b)) >= 0;
    }

    /** max(v, 0.00) — reemplaza Math.max(0, ...) sobre dinero. */
    public static BigDecimal maxZero(BigDecimal v) {
        return gt(v, zero()) ? scale(v) : zero();
    }
}
