package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.DetallePedido;
import tics.uide.gestionuide.model.Pedido;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.repository.DetallePedidoRepository;

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
