package tics.uide.gestionuide.service;

import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.CrearReservaDto;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.enums.EstadoReserva;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Mesa;
import tics.uide.gestionuide.model.Reserva;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.repository.ReservaRepository;

@Service
@Transactional
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private MesaService mesaService;
    @Autowired
    private UsuarioService usuarioService;

    public Reserva crear(CrearReservaDto dto, Long usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Mesa mesa = mesaService.buscarPorId(dto.getMesaId());

        if (!mesaDisponible(dto.getMesaId(), dto.getFechaReserva(), dto.getDuracionHoras())) {
            throw new BadRequestException("La mesa no está disponible para esa fecha y hora");
        }

        Reserva reserva = Reserva.builder()
                .usuario(usuario).mesa(mesa)
                .fechaReserva(dto.getFechaReserva())
                .duracionHoras(dto.getDuracionHoras() != null ? dto.getDuracionHoras() : 2)
                .numPersonas(dto.getNumPersonas())
                .estado(EstadoReserva.PENDIENTE)
                .notas(dto.getNotas()).build();

        return reservaRepository.save(reserva);
    }

    public Reserva cambiarEstado(Long id, EstadoReserva nuevoEstado) {
        Reserva reserva = buscarPorId(id);
        if (nuevoEstado == EstadoReserva.CONFIRMADA && reserva.getMesa() != null) {
            mesaService.cambiarEstado(reserva.getMesa().getId(), EstadoMesa.RESERVADA);
        }
        if (nuevoEstado == EstadoReserva.CANCELADA && reserva.getMesa() != null) {
            mesaService.cambiarEstado(reserva.getMesa().getId(), EstadoMesa.LIBRE);
        }
        if (nuevoEstado == EstadoReserva.COMPLETADA && reserva.getMesa() != null) {
            mesaService.cambiarEstado(reserva.getMesa().getId(), EstadoMesa.LIBRE);
        }
        reserva.setEstado(nuevoEstado);
        return reservaRepository.save(reserva);
    }

    public Reserva confirmar(Long id) { return cambiarEstado(id, EstadoReserva.CONFIRMADA); }
    public Reserva cancelar(Long id) { return cambiarEstado(id, EstadoReserva.CANCELADA); }
    public Reserva completar(Long id) { return cambiarEstado(id, EstadoReserva.COMPLETADA); }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada con ID: " + id));
    }

    public List<Reserva> listarTodas() { return reservaRepository.findAll(); }
    public org.springframework.data.domain.Page<Reserva> listarTodas(org.springframework.data.domain.Pageable pageable) { return reservaRepository.findAll(pageable); }
    public List<Reserva> listarPorUsuario(Long uid) { return reservaRepository.findByUsuario_Id(uid); }
    public List<Reserva> listarPorMesa(Long mid) { return reservaRepository.findByMesa_Id(mid); }
    public List<Reserva> listarPorEstado(EstadoReserva e) { return reservaRepository.findByEstado(e); }

    public boolean mesaDisponible(Long mesaId, Date fecha, Integer duracion) {
        Mesa mesa = mesaService.buscarPorId(mesaId);
        if (mesa.getEstado() == EstadoMesa.OCUPADA) return false;
        List<Reserva> conflictos = reservaRepository.findByMesa_IdAndEstadoAndFechaReservaBetween(
                mesaId, EstadoReserva.CONFIRMADA,
                new Date(fecha.getTime() - (duracion * 3600000L)),
                new Date(fecha.getTime() + (duracion * 3600000L)));
        return conflictos.isEmpty();
    }
}
