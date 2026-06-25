package com.cafeteria.app.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.enums.EstadoReserva;
import com.cafeteria.app.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario_Id(Long usuarioId);
    List<Reserva> findByMesa_Id(Long mesaId);
    List<Reserva> findByEstado(EstadoReserva estado);
    List<Reserva> findByMesa_IdAndEstadoAndFechaReservaBetween(Long mesaId, EstadoReserva estado, Date inicio, Date fin);
}
