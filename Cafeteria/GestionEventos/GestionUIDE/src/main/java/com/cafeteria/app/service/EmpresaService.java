package com.cafeteria.app.service;

import java.math.BigDecimal;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.dto.EmpresaDto;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.Empresa;
import com.cafeteria.app.repository.EmpresaRepository;
import com.cafeteria.app.util.Money;

@Service
@Transactional
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private IvaService ivaService;

    public Empresa crear(EmpresaDto dto) {
        if (empresaRepository.existsById(dto.getRuc())) {
            throw new BadRequestException("Ya existe una empresa con el RUC: " + dto.getRuc());
        }

        if (empresaRepository.existsByNombre(dto.getNombre())) {
            throw new BadRequestException("Ya existe una empresa con el nombre: " + dto.getNombre());
        }

        Empresa empresa = Empresa.builder()
                .ruc(dto.getRuc())
                .nombre(dto.getNombre())
                .nombreComercial(dto.getNombreComercial())
                .iva(dto.getIva() != null ? Money.scale(dto.getIva()) : ivaService.porcentajePorDefecto())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .logoUrl(dto.getLogoUrl())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();

        return empresaRepository.save(empresa);
    }

    public Empresa actualizar(String ruc, EmpresaDto dto) {
        Empresa empresa = buscarPorRuc(ruc);

        if (!empresa.getNombre().equals(dto.getNombre())) {
            if (empresaRepository.existsByNombre(dto.getNombre())) {
                throw new BadRequestException("Ya existe una empresa con el nombre: " + dto.getNombre());
            }
            empresa.setNombre(dto.getNombre());
        }

        empresa.setNombreComercial(dto.getNombreComercial());
        empresa.setIva(Money.scale(dto.getIva()));
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setEmail(dto.getEmail());
        empresa.setLogoUrl(dto.getLogoUrl());
        empresa.setDescripcion(dto.getDescripcion());

        return empresaRepository.save(empresa);
    }

    public void eliminar(String ruc) {
        Empresa empresa = buscarPorRuc(ruc);
        empresaRepository.delete(empresa);
    }

    public void desactivar(String ruc) {
        Empresa empresa = buscarPorRuc(ruc);
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }

    public Empresa buscarPorRuc(String ruc) {
        return empresaRepository.findById(ruc)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada con RUC: " + ruc));
    }

    public Empresa buscarPorNombre(String nombre) {
        return empresaRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada: " + nombre));
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    public List<Empresa> listarActivas() {
        return empresaRepository.findByActivoTrue();
    }

    public Empresa actualizarIva(String ruc, BigDecimal nuevoIva) {
        Empresa empresa = buscarPorRuc(ruc);
        empresa.setIva(Money.scale(nuevoIva));
        return empresaRepository.save(empresa);
    }
}
