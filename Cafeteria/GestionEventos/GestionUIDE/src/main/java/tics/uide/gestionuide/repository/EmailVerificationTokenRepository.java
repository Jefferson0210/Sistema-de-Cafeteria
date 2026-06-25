package tics.uide.gestionuide.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.model.EmailVerificationToken;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    List<EmailVerificationToken> findByUsuario_IdAndUsadoFalse(Long usuarioId);

    /** Purga de tokens caducados antes del cutoff (job de limpieza). Devuelve filas borradas. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiraEn < :cutoff")
    int deleteExpiradosAntesDe(@Param("cutoff") Date cutoff);
}
