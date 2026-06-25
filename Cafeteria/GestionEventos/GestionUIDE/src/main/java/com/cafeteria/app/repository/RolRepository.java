package com.cafeteria.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.enums.Rol;
import com.cafeteria.app.model.Roles;
import com.cafeteria.app.model.Usuario;

@Repository
public interface RolRepository extends JpaRepository<Roles, Long> {
    List<Roles> findByUsuario(Usuario usuario);
    List<Roles> findByUsuario_Id(Long usuarioId);
    List<Roles> findByUsuarioAndRol(Usuario usuario, Rol rol);
    boolean existsByUsuarioAndRol(Usuario usuario, Rol rol);
    boolean existsByUsuario_IdAndRol(Long usuarioId, Rol rol);
}
