package com.cafeteria.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.model.Category;
import com.cafeteria.app.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByNombre(String nombre);
    List<Producto> findByDisponibleTrue();
    List<Producto> findByCategory(Category category);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByNombre(String nombre);
}
