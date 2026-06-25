package com.cafeteria.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Usuario",
        indexes = {
            @Index(name = "idx_usuario_username", columnList = "username"),
            @Index(name = "idx_usuario_email", columnList = "email")
        })
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    // NUEVO: foto de perfil
    @Column(length = 500)
    private String fotoUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // Doble opt-in: ¿confirmó su correo? (separado de 'activo', que es control de admin)
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerificado = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaRegistro;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Roles> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Pedido> pedidosComoCliente;

    @JsonIgnore
    @OneToMany(mappedBy = "mesero", fetch = FetchType.LAZY)
    private List<Pedido> pedidosComoMesero;

    @JsonIgnore
    @OneToMany(mappedBy = "cajero", fetch = FetchType.LAZY)
    private List<Factura> facturasEmitidas;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Factura> facturasRecibidas;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = new Date();
        fechaActualizacion = new Date();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}
