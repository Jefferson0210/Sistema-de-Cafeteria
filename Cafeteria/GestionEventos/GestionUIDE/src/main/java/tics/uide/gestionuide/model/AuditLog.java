package tics.uide.gestionuide.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "AuditLog", indexes = {
        @Index(name = "idx_audit_fecha", columnList = "fecha"),
        @Index(name = "idx_audit_usuario", columnList = "usuario")
})
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;      // quién (username del token)

    @Column(nullable = false, length = 50)
    private String accion;       // p.ej. PAGO_REGISTRADO

    @Column(length = 50)
    private String entidad;      // p.ej. Factura

    private Long entidadId;      // id afectado

    @Column(length = 500)
    private String detalle;      // opcional

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fecha;

    @PrePersist
    protected void onCreate() {
        fecha = new Date();
    }
}
