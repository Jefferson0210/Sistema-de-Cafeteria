package tics.uide.gestionuide.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.PageMeta;
import tics.uide.gestionuide.model.AuditLog;
import tics.uide.gestionuide.repository.AuditLogRepository;
import tics.uide.gestionuide.util.PageUtils;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/auditoria")
public class AuditWS {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha,desc") String sort) {
        if (page == null) {
            return ResponseEntity.ok(new ApiResponse(true, "Bitácora de auditoría",
                    auditLogRepository.findAllByOrderByFechaDesc()));
        }
        Page<AuditLog> p = auditLogRepository.findAll(PageUtils.of(page, size, sort));
        return ResponseEntity.ok(new ApiResponse(true, "Bitácora de auditoría", p.getContent(), new PageMeta(p)));
    }
}
