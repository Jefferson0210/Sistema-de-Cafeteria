package com.cafeteria.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.model.DetalleFactura;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {
    List<DetalleFactura> findByFactura_Id(Long facturaId);
}
