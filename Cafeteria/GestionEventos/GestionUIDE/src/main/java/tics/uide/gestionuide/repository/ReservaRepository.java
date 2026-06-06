package tics.uide.gestionuide.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.enums.EstadoReserva;
import tics.uide.gestionuide.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario_Id(Long usuarioId);
    List<Reserva> findByMesa_Id(Long mesaId);
    List<Reserva> findByEstado(EstadoReserva estado);
    List<Reserva> findByMesa_IdAndEstadoAndFechaReservaBetween(Long mesaId, EstadoReserva estado, Date inicio, Date fin);
}
