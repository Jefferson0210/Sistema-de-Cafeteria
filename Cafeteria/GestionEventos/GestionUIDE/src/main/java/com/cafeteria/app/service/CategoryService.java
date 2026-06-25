package com.cafeteria.app.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.dto.CategoryDto;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.Category;
import com.cafeteria.app.repository.CategoryRepository;
import com.cafeteria.app.repository.ProductoRepository;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Category crear(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + dto.getName());
        }
        Category category = Category.builder()
                .name(dto.getName())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
        return categoryRepository.save(category);
    }

    public Category actualizar(Long id, CategoryDto dto) {
        Category category = buscarPorId(id);
        if (!category.getName().equals(dto.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new BadRequestException("Ya existe una categoría con el nombre: " + dto.getName());
            }
            category.setName(dto.getName());
        }
        category.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            category.setActivo(dto.getActivo());
        }
        return categoryRepository.save(category);
    }

    public void eliminar(Long id) {
        Category category = buscarPorId(id);
        productoRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getCategoryId().equals(id))
                .forEach(p -> {
                    p.setCategory(null);
                    productoRepository.save(p);
                });
        categoryRepository.delete(category);
    }

    public void desactivar(Long id) {
        Category category = buscarPorId(id);
        category.setActivo(false);
        categoryRepository.save(category);
    }

    public void activar(Long id) {
        Category category = buscarPorId(id);
        category.setActivo(true);
        categoryRepository.save(category);
    }

    public Category buscarPorId(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + id));
    }

    public Category buscarPorNombre(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + name));
    }

    public List<Category> listarTodas() {
        return categoryRepository.findAll();
    }

    public List<Category> listarActivas() {
        return categoryRepository.findByActivoTrue();
    }
}