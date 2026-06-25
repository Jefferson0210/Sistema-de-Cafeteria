package tics.uide.gestionuide.repository;

import java.util.Collection;
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

    // ----- Sesión de mesa (cuenta por QR) -----
    /** Pedidos ABIERTOS de una mesa (estado no en {cerrados}). */
    List<Pedido> findByMesa_IdAndEstadoNotIn(Long mesaId, Collection<EstadoPedido> cerrados);
    /** ¿Quedan pedidos abiertos en la mesa? (para liberar consciente del modo). */
    boolean existsByMesa_IdAndEstadoNotIn(Long mesaId, Collection<EstadoPedido> cerrados);
    /** Pedido(s) abierto(s) de un cliente concreto en una mesa (modo SEPARADA). */
    List<Pedido> findByMesa_IdAndCliente_IdAndEstadoNotIn(Long mesaId, Long clienteId, Collection<EstadoPedido> cerrados);
}
