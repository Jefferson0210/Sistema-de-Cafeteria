package tics.uide.gestionuide.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.enums.EstadoPedido;
import tics.uide.gestionuide.model.Mesa;
import tics.uide.gestionuide.model.Pedido;
import tics.uide.gestionuide.model.Usuario;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByMesa(Mesa mesa);
    List<Pedido> findByCliente(Usuario cliente);
    List<Pedido> findByMesero(Usuario mesero);
    List<Pedido> findByEstado(EstadoPedido estado);
}
