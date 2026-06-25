package tics.uide.gestionuide.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.PagoDto;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.enums.MetodoPago;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Factura;
import tics.uide.gestionuide.model.Pago;
import tics.uide.gestionuide.repository.PagoRepository;
import tics.uide.gestionuide.util.Money;

@Service
@Transactional
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private AuditService auditService;

    public Pago registrar(PagoDto dto) {
        Factura factura = facturaService.buscarPorId(dto.getFacturaId());

        // 1) No se paga sobre facturas ya pagadas o canceladas
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new BadRequestException("La factura ya está pagada");
        }
        if (factura.getEstado() == EstadoFactura.CANCELADA) {
            throw new BadRequestException("No se puede registrar un pago sobre una factura cancelada");
        }

        // 2) Monto válido
        if (dto.getMonto() == null || dto.getMonto().signum() <= 0) {
            throw new BadRequestException("El monto debe ser positivo");
        }

        // 3) Idempotencia: referencia única por factura (cuando se provee)
        String referencia = dto.getReferencia() != null ? dto.getReferencia().trim() : null;
        if (referencia != null && !referencia.isEmpty()
                && pagoRepository.existsByFactura_IdAndReferencia(factura.getId(), referencia)) {
            throw new BadRequestException(
                    "Ya existe un pago con la referencia '" + referencia + "' para esta factura");
        }

        // 4) Tope al saldo pendiente (no se puede pagar de más)
        BigDecimal monto = Money.scale(dto.getMonto());
        BigDecimal pendiente = Money.subtract(factura.getTotal(), calcularTotalPagado(factura.getId()));
        if (Money.gt(monto, pendiente)) {
            throw new BadRequestException(String.format(
                    "El monto %.2f excede el saldo pendiente %.2f de la factura",
                    monto, Money.maxZero(pendiente)));
        }

        Pago pago = Pago.builder()
                .factura(factura)
                .metodoPago(dto.getMetodoPago())
                .monto(monto)
                .referencia(referencia)
                .build();
        pago = pagoRepository.save(pago);
        auditService.registrar("PAGO_REGISTRADO", "Pago", pago.getId(),
                "factura=" + factura.getId() + ", metodo=" + pago.getMetodoPago() + ", monto=" + pago.getMonto());

        // 5) TARJETA y TRANSFERENCIA sin referencia válida registran el pago pero
        //    NO confirman la factura como PAGADA. EFECTIVO confirma directo.
        boolean requiereReferencia = dto.getMetodoPago() == MetodoPago.TARJETA
                || dto.getMetodoPago() == MetodoPago.TRANSFERENCIA;
        boolean pagoConfirmado = !(requiereReferencia && (referencia == null || referencia.isEmpty()));
        if (pagoConfirmado && facturaEstaPagada(factura.getId())) {
            facturaService.cambiarEstado(factura.getId(), EstadoFactura.PAGADA);
        }

        return pago;
    }

    public Pago registrarEfectivo(Long facturaId, BigDecimal monto) {
        PagoDto dto = PagoDto.builder()
                .facturaId(facturaId)
                .metodoPago(MetodoPago.EFECTIVO)
                .monto(monto)
                .build();
        return registrar(dto);
    }

    public Pago registrarTarjeta(Long facturaId, BigDecimal monto, String referencia) {
        PagoDto dto = PagoDto.builder()
                .facturaId(facturaId)
                .metodoPago(MetodoPago.TARJETA)
                .monto(monto)
                .referencia(referencia)
                .build();
        return registrar(dto);
    }

    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado con ID: " + id));
    }

    public List<Pago> listarPorFactura(Long facturaId) {
        return pagoRepository.findByFactura_Id(facturaId);
    }

    public List<Pago> listarPorMetodo(MetodoPago metodo) {
        return pagoRepository.findByMetodoPago(metodo);
    }

    public BigDecimal calcularTotalPagado(Long facturaId) {
        return Money.sum(listarPorFactura(facturaId).stream()
                .map(Pago::getMonto).collect(Collectors.toList()));
    }

    public boolean facturaEstaPagada(Long facturaId) {
        Factura factura = facturaService.buscarPorId(facturaId);
        return Money.gte(calcularTotalPagado(facturaId), factura.getTotal());
    }

    public BigDecimal calcularPendiente(Long facturaId) {
        Factura factura = facturaService.buscarPorId(facturaId);
        return Money.maxZero(Money.subtract(factura.getTotal(), calcularTotalPagado(facturaId)));
    }
}
