package com.cafeteria.app.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.DetallePedido;
import com.cafeteria.app.model.Pedido;
import com.cafeteria.app.model.Producto;
import com.cafeteria.app.repository.DetallePedidoRepository;

@Service
@Transactional
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    public DetallePedido crear(Pedido pedido, Producto producto, Integer cantidad, String notas) {
        DetallePedido detalle = DetallePedido.builder()
                .pedido(pedido)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(producto.getPrecio())
                .notas(notas)
                .build();

        return detallePedidoRepository.save(detalle);
    }

    public DetallePedido actualizar(Long id, Integer nuevaCantidad) {
        DetallePedido detalle = buscarPorId(id);
        detalle.setCantidad(nuevaCantidad);
        return detallePedidoRepository.save(detalle);
    }

    public void eliminar(Long id) {
        DetallePedido detalle = buscarPorId(id);
        detallePedidoRepository.delete(detalle);
    }

    public DetallePedido buscarPorId(Long id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Detalle de pedido no encontrado con ID: " + id));
    }

    public List<DetallePedido> listarPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedido_Id(pedidoId);
    }
}
