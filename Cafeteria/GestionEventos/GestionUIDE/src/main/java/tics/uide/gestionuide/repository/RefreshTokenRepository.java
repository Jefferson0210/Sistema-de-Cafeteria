package tics.uide.gestionuide.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUsuario_IdAndRevocadoFalse(Long usuarioId);

    /** Purga de refresh tokens caducados antes del cutoff (job de limpieza). Devuelve filas borradas. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken t WHERE t.expiraEn < :cutoff")
    int deleteExpiradosAntesDe(@Param("cutoff") Date cutoff);
}
