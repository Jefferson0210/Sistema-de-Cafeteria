package com.cafeteria.app.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.dto.MesaDto;
import com.cafeteria.app.enums.EstadoMesa;
import com.cafeteria.app.enums.ModoCuenta;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.Mesa;
import com.cafeteria.app.repository.MesaRepository;

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

    /** Fija el modo de cuenta de la sesión (primer escaneo). */
    public Mesa fijarModo(Long id, ModoCuenta modo) {
        Mesa mesa = buscarPorId(id);
        mesa.setModoCuenta(modo);
        return mesaRepository.save(mesa);
    }

    /** Cierra la sesión: libera la mesa (LIBRE) y resetea el modo de cuenta. */
    public Mesa liberar(Long id) {
        Mesa mesa = buscarPorId(id);
        mesa.setEstado(EstadoMesa.LIBRE);
        mesa.setModoCuenta(null);
        return mesaRepository.save(mesa);
    }
}
