package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.MesaDto;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Mesa;
import tics.uide.gestionuide.repository.MesaRepository;

/**
 * Servicio de Mesas - CORREGIDO: añadido CRUD completo
 */
@Service
@Transactional
public class MesaService {

    @Autowired
    private MesaRepository mesaRepository;

    public Mesa crear(MesaDto dto) {
        if (mesaRepository.existsByNumeroMesa(dto.getNumeroMesa())) {
            throw new BadRequestException("Ya existe una mesa con el número: " + dto.getNumeroMesa());
        }

        Mesa mesa = Mesa.builder()
                .numeroMesa(dto.getNumeroMesa())
                .capacidad(dto.getCapacidad())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoMesa.LIBRE)
                .ubicacion(dto.getUbicacion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        return mesaRepository.save(mesa);
    }

    public Mesa actualizar(Long id, MesaDto dto) {
        Mesa mesa = buscarPorId(id);

        if (!mesa.getNumeroMesa().equals(dto.getNumeroMesa())
                && mesaRepository.existsByNumeroMesa(dto.getNumeroMesa())) {
            throw new BadRequestException("Ya existe una mesa con el número: " + dto.getNumeroMesa());
        }

        mesa.setNumeroMesa(dto.getNumeroMesa());
        mesa.setCapacidad(dto.getCapacidad());
        if (dto.getUbicacion() != null) mesa.setUbicacion(dto.getUbicacion());
        if (dto.getActivo() != null) mesa.setActivo(dto.getActivo());

        return mesaRepository.save(mesa);
    }

    public void eliminar(Long id) {
        Mesa mesa = buscarPorId(id);
        if (mesa.getEstado() == EstadoMesa.OCUPADA) {
            throw new BadRequestException("No se puede eliminar una mesa ocupada");
        }
        mesaRepository.delete(mesa);
    }

    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mesa no encontrada con ID: " + id));
    }

    public List<Mesa> listarTodas() {
        return mesaRepository.findAll();
    }

    public List<Mesa> listarDisponibles() {
        return mesaRepository.findByEstado(EstadoMesa.LIBRE);
    }

    public List<Mesa> listarPorEstado(EstadoMesa estado) {
        return mesaRepository.findByEstado(estado);
    }

    public Mesa cambiarEstado(Long id, EstadoMesa nuevoEstado) {
        Mesa mesa = buscarPorId(id);

        // Validar transición: OCUPADA → RESERVADA no permitida
        if (mesa.getEstado() == EstadoMesa.OCUPADA && nuevoEstado == EstadoMesa.RESERVADA) {
            throw new BadRequestException("No se puede reservar una mesa que está ocupada");
        }

        mesa.setEstado(nuevoEstado);
        return mesaRepository.save(mesa);
    }
}
