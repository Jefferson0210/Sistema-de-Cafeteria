package tics.uide.gestionuide.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.model.Factura;
import tics.uide.gestionuide.model.Usuario;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    List<Factura> findByCliente(Usuario cliente);
    List<Factura> findByCajero(Usuario cajero);
    List<Factura> findByEstado(EstadoFactura estado);
}
