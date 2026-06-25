package com.cafeteria.app.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "EmailVerificationToken")
public class EmailVerificationToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SHA-256 (hex) del token; nunca se guarda el token crudo
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "EMAILVERIF_USUARIO_FK"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiraEn;

    @Column(nullable = false)
    private boolean usado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
    }
}
