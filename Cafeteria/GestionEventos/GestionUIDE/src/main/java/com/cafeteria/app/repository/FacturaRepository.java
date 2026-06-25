package com.cafeteria.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.enums.EstadoFactura;
import com.cafeteria.app.model.Factura;
import com.cafeteria.app.model.Usuario;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    List<Factura> findByCliente(Usuario cliente);
    List<Factura> findByCajero(Usuario cajero);
    List<Factura> findByEstado(EstadoFactura estado);
}
