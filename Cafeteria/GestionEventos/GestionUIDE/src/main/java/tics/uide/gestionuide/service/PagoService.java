package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.PagoDto;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.enums.MetodoPago;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Factura;
import tics.uide.gestionuide.model.Pago;
import tics.uide.gestionuide.repository.PagoRepository;

@Service
@Transactional
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FacturaService facturaService;

    public Pago registrar(PagoDto dto) {
        Factura factura = facturaService.buscarPorId(dto.getFacturaId());

        Pago pago = Pago.builder()
                .factura(factura)
                .metodoPago(dto.getMetodoPago())
                .monto(dto.getMonto())
                .referencia(dto.getReferencia())
                .build();

        pago = pagoRepository.save(pago);

        if (facturaEstaPagada(factura.getId())) {
            facturaService.cambiarEstado(factura.getId(), EstadoFactura.PAGADA);
        }

        return pago;
    }

    public Pago registrarEfectivo(Long facturaId, Double monto) {
        PagoDto dto = PagoDto.builder()
                .facturaId(facturaId)
                .metodoPago(MetodoPago.EFECTIVO)
                .monto(monto)
                .build();
        return registrar(dto);
    }

    public Pago registrarTarjeta(Long facturaId, Double monto, String referencia) {
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

    public Double calcularTotalPagado(Long facturaId) {
        List<Pago> pagos = listarPorFactura(facturaId);
        return pagos.stream().mapToDouble(Pago::getMonto).sum();
    }

    public boolean facturaEstaPagada(Long facturaId) {
        Factura factura = facturaService.buscarPorId(facturaId);
        Double totalPagado = calcularTotalPagado(facturaId);
        return totalPagado >= factura.getTotal();
    }

    public Double calcularPendiente(Long facturaId) {
        Factura factura = facturaService.buscarPorId(facturaId);
        Double totalPagado = calcularTotalPagado(facturaId);
        return Math.max(0, factura.getTotal() - totalPagado);
    }
}
