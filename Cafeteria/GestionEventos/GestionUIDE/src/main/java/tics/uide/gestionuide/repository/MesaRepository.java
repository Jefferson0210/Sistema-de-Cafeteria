package tics.uide.gestionuide.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.model.Mesa;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumeroMesa(Integer numeroMesa);
    List<Mesa> findByEstado(EstadoMesa estado);
    List<Mesa> findByActivoTrue();
    List<Mesa> findByCapacidadGreaterThanEqualAndActivoTrue(Integer capacidad);
    List<Mesa> findByEstadoAndCapacidadGreaterThanEqual(EstadoMesa estado, Integer capacidad);
    boolean existsByNumeroMesa(Integer numeroMesa);
}
