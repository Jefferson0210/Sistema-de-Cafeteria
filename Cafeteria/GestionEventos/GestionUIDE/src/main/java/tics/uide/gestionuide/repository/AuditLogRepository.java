package tics.uide.gestionuide.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tics.uide.gestionuide.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByFechaDesc();
}
