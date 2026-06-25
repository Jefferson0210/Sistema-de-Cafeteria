package tics.uide.gestionuide.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.util.Money;

/** Fuente única de la tasa de IVA y de su cálculo. */
@Service
public class IvaService {

    /** Porcentaje por defecto (ej. 15 = 15%). Configurable por entorno. */
    @Value("${app.iva.default-percent:15}")
    private BigDecimal porcentajePorDefecto;

    public BigDecimal porcentajePorDefecto() {
        return porcentajePorDefecto;
    }

    /** Convierte un porcentaje (15) a fracción (0.15) con escala amplia. */
    public BigDecimal fraccion(BigDecimal porcentaje) {
        return Money.nz(porcentaje).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    /** Monto de IVA sobre una base con el porcentaje dado, redondeado a 2. */
    public BigDecimal calcular(BigDecimal base, BigDecimal porcentaje) {
        return Money.scale(Money.nz(base).multiply(fraccion(porcentaje)));
    }

    /** IVA con la tasa por defecto. */
    public BigDecimal calcularPorDefecto(BigDecimal base) {
        return calcular(base, porcentajePorDefecto);
    }
}
