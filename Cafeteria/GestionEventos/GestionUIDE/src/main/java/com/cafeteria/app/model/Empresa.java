package com.cafeteria.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import com.cafeteria.app.util.Money;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad Empresa - CORREGIDA para acceso público a IVA
 */
@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Empresa")
public class Empresa implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(nullable = false, length = 13, unique = true)
    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 10, max = 13, message = "El RUC debe tener entre 10 y 13 caracteres")
    private String ruc;
    
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100, unique = true)
    private String nombre;
    
    @Column(length = 100)
    private String nombreComercial;
    
    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal iva = Money.of(15.0);
    
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(length = 200)
    private String direccion;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;
    
    @Email(message = "El email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;
    
    @Column(length = 255)
    private String logoUrl;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String descripcion;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaCreacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;
    
    @JsonIgnore
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Factura> facturas;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
        fechaActualizacion = new Date();
        if (activo == null) {
            activo = true;
        }
        if (iva == null) {
            iva = Money.of(15.0);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
    
    // Getter manual para iva (por si Lombok falla)
    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }
}