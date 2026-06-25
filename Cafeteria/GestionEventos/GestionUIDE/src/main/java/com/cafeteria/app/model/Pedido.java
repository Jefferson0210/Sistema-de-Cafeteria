package com.cafeteria.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.cafeteria.app.enums.EstadoPedido;
import com.cafeteria.app.util.Money;

@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Pedido",
        indexes = {
            @Index(name = "idx_pedido_estado", columnList = "estado"),
            @Index(name = "idx_pedido_fecha", columnList = "fechaPedido")
        })
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa", foreignKey = @ForeignKey(name = "PEDIDO_MESA_FK"))
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", foreignKey = @ForeignKey(name = "PEDIDO_CLIENTE_FK"))
    private Usuario cliente;

    // CORREGIDO: mesero ya no es @NotNull (opcional para pedidos de cliente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesero", foreignKey = @ForeignKey(name = "PEDIDO_MESERO_FK"))
    private Usuario mesero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(length = 500)
    private String notas;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaPedido;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;

    @JsonProperty("fechaCreacion")
    public Date getFechaCreacion() {
        return fechaPedido;
    }

    // Detalles visibles en JSON (no @JsonIgnore)
@OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
private List<DetallePedido> detalles;

    @JsonIgnore
    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
    private Factura factura;

    @PrePersist
    protected void onCreate() {
        fechaPedido = new Date();
        fechaActualizacion = new Date();
        if (estado == null) estado = EstadoPedido.PENDIENTE;
        if (subtotal == null) subtotal = Money.zero();
        if (iva == null) iva = Money.zero();
        if (total == null) total = Money.zero();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}
