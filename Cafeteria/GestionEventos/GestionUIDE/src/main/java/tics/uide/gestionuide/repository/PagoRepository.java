package tics.uide.gestionuide.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.enums.MetodoPago;
import tics.uide.gestionuide.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByFactura_Id(Long facturaId);
    List<Pago> findByMetodoPago(MetodoPago metodoPago);
    boolean existsByFactura_IdAndReferencia(Long facturaId, String referencia);
}
