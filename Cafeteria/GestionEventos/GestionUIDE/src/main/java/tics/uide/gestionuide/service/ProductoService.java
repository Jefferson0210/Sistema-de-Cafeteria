package tics.uide.gestionuide.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tics.uide.gestionuide.dto.ProductoDto;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Category;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.repository.ProductoRepository;
import tics.uide.gestionuide.util.Money;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuditService auditService;

    public Producto crear(ProductoDto dto) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryService.buscarPorId(dto.getCategoryId());
        }

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(Money.scale(dto.getPrecio()))
                .category(category)
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .disponible(true)
                .imagenUrl(dto.getImagenUrl())
                .build();

        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, ProductoDto dto) {
        Producto producto = buscarPorId(id);

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(Money.scale(dto.getPrecio()));

        if (dto.getCategoryId() != null) {
            Category category = categoryService.buscarPorId(dto.getCategoryId());
            producto.setCategory(category);
        }

        if (dto.getStock() != null) {
            producto.setStock(dto.getStock());
        }

        producto.setImagenUrl(dto.getImagenUrl());

        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
        auditService.registrar("PRODUCTO_ELIMINADO", "Producto", id, "nombre=" + producto.getNombre());
    }

    public Producto actualizarImagen(Long id, MultipartFile archivo) throws IOException {
        Producto producto = buscarPorId(id);
        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
        Path ruta = Paths.get("uploads/productos").resolve(nombreArchivo);
        Files.createDirectories(ruta.getParent());
        Files.copy(archivo.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);
        producto.setImagenUrl("/uploads/productos/" + nombreArchivo);
        return productoRepository.save(producto);
    }

    public void desactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setDisponible(false);
        productoRepository.save(producto);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    public Producto buscarPorNombre(String nombre) {
        return productoRepository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + nombre));
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public org.springframework.data.domain.Page<Producto> listarTodos(org.springframework.data.domain.Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    public List<Producto> listarDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }

    public List<Producto> buscarPorCategoria(Long categoryId) {
        Category category = categoryService.buscarPorId(categoryId);
        return productoRepository.findByCategory(category);
    }

    public List<Producto> buscarPorNombreContiene(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Producto actualizarStock(Long id, Integer cantidadAgregar) {
        Producto producto = buscarPorId(id);
        producto.setStock(producto.getStock() + cantidadAgregar);

        if (producto.getStock() > 0 && !producto.getDisponible()) {
            producto.setDisponible(true);
        }

        return productoRepository.save(producto);
    }

    public Producto reducirStock(Long id, Integer cantidadReducir) {
        Producto producto = buscarPorId(id);

        if (producto.getStock() < cantidadReducir) {
            throw new BadRequestException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidadReducir);

        if (producto.getStock() == 0) {
            producto.setDisponible(false);
        }

        return productoRepository.save(producto);
    }

    public boolean hayStock(Long id, Integer cantidadRequerida) {
        Producto producto = buscarPorId(id);
        return producto.getStock() >= cantidadRequerida;
    }
}