package tics.uide.gestionuide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.EstadoPedido;

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
    private Double subtotal;

    @Column(precision = 10, scale = 2)
    private Double iva;

    @Column(precision = 10, scale = 2)
    private Double total;

    @Column(length = 500)
    private String notas;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaPedido;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;

    // NUEVO: el frontend usa "fechaCreacion", este alias lo expone correctamente
    @JsonProperty("fechaCreacion")
    public Date getFechaCreacion() {
        return fechaPedido;
    }

    // Detalles visibles en JSON (no @JsonIgnore)
    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;

    @JsonIgnore
    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
    private Factura factura;

    @PrePersist
    protected void onCreate() {
        fechaPedido = new Date();
        fechaActualizacion = new Date();
        if (estado == null) estado = EstadoPedido.PENDIENTE;
        if (subtotal == null) subtotal = 0.0;
        if (iva == null) iva = 0.0;
        if (total == null) total = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}
