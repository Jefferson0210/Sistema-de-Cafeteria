package com.cafeteria.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.model.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    Optional<Empresa> findByNombre(String nombre);
    List<Empresa> findByActivoTrue();
    boolean existsByNombre(String nombre);
}
