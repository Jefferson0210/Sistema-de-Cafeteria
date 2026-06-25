package com.cafeteria.app.service;

import java.math.BigDecimal;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.DetalleFactura;
import com.cafeteria.app.model.Factura;
import com.cafeteria.app.model.Producto;
import com.cafeteria.app.repository.DetalleFacturaRepository;
import com.cafeteria.app.util.Money;

@Service
@Transactional
public class DetalleFacturaService {

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    public DetalleFactura crear(Factura factura, Producto producto, Double cantidad) {
        DetalleFactura detalle = DetalleFactura.builder()
                .factura(factura)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(producto.getPrecio())
                .subtotal(Money.multiply(producto.getPrecio(), BigDecimal.valueOf(cantidad)))
                .build();

        return detalleFacturaRepository.save(detalle);
    }

    public DetalleFactura actualizar(Long id, Double nuevaCantidad) {
        DetalleFactura detalle = buscarPorId(id);
        detalle.setCantidad(nuevaCantidad);
        detalle.setSubtotal(Money.multiply(detalle.getPrecioUnitario(), BigDecimal.valueOf(nuevaCantidad)));
        return detalleFacturaRepository.save(detalle);
    }

    public void eliminar(Long id) {
        DetalleFactura detalle = buscarPorId(id);
        detalleFacturaRepository.delete(detalle);
    }

    public DetalleFactura buscarPorId(Long id) {
        return detalleFacturaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Detalle de factura no encontrado con ID: " + id));
    }

    public List<DetalleFactura> listarPorFactura(Long facturaId) {
        return detalleFacturaRepository.findByFactura_Id(facturaId);
    }
}
