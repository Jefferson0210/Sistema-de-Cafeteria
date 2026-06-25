package com.cafeteria.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cafeteria.app.enums.Rol;
import com.cafeteria.app.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Usuario> findByActivoTrue();
    List<Usuario> findByRoles_Rol(Rol rol);
}
