package tics.uide.gestionuide.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.model.Favoritos;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.model.Usuario;

@Repository
public interface FavoritosRepository extends JpaRepository<Favoritos, Long> {
    List<Favoritos> findByUsuario_Id(Long usuarioId);
    Optional<Favoritos> findByUsuarioAndProducto(Usuario usuario, Producto producto);
    boolean existsByUsuarioAndProducto(Usuario usuario, Producto producto);
}
