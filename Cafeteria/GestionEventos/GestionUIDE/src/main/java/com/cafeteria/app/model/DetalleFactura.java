package com.cafeteria.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Detalle de Factura - CORREGIDO
 */
@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Detalles")
public class DetalleFactura implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    private Double cantidad;            // cantidad: no es dinero, se mantiene Double

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @ManyToOne
    @JoinColumn(name = "producto", nullable = false, foreignKey = @ForeignKey(name = "PRODUCTO_DETALLE_FK"))
    private Producto producto;
    
    @ManyToOne
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Factura factura;
    
    @JsonIgnore
    @Transient
    private String nombre;
    
    @Transient
    public String getNombreProducto() {
        return producto != null ? producto.getNombre() : null;
    }
    
    // Getter manual para nombre (por si Lombok falla)
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}