package com.cafeteria.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.enums.EstadoMesa;
import com.cafeteria.app.model.Mesa;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumeroMesa(Integer numeroMesa);
    List<Mesa> findByEstado(EstadoMesa estado);
    List<Mesa> findByActivoTrue();
    List<Mesa> findByCapacidadGreaterThanEqualAndActivoTrue(Integer capacidad);
    List<Mesa> findByEstadoAndCapacidadGreaterThanEqual(EstadoMesa estado, Integer capacidad);
    boolean existsByNumeroMesa(Integer numeroMesa);
}
