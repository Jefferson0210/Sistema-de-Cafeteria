package tics.uide.gestionuide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.util.Money;

@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Factura",
        indexes = {
            @Index(name = "idx_factura_numero", columnList = "numeroFactura"),
            @Index(name = "idx_factura_fecha", columnList = "fechaEmision"),
            @Index(name = "idx_factura_estado", columnList = "estado")
        })
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long id;

    @NotBlank(message = "El número de factura es obligatorio")
    @Column(nullable = false, unique = true, length = 50)
    private String numeroFactura;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", foreignKey = @ForeignKey(name = "FACTURA_PEDIDO_FK"))
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", foreignKey = @ForeignKey(name = "FACTURA_CLIENTE_FK"))
    private Usuario cliente;

    @NotNull(message = "El cajero es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cajero", nullable = false, foreignKey = @ForeignKey(name = "FACTURA_CAJERO_FK"))
    private Usuario cajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruc_empresa", foreignKey = @ForeignKey(name = "FACTURA_EMPRESA_FK"))
    private Empresa empresa;

    // CORREGIDO: quitado @Positive para permitir 0.0 inicial en crearManual
    @NotNull(message = "El subtotal es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "El IVA es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal descuento = Money.zero();

    @NotNull(message = "El total es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    @Column(length = 500)
    private String notas;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaEmision;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;

    @OneToMany(mappedBy = "factura", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFactura> detalles;

    @JsonIgnore
    @OneToMany(mappedBy = "factura", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos;

    @PrePersist
    protected void onCreate() {
        fechaEmision = new Date();
        fechaActualizacion = new Date();
        if (estado == null) estado = EstadoFactura.PENDIENTE;
        if (descuento == null) descuento = Money.zero();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}
