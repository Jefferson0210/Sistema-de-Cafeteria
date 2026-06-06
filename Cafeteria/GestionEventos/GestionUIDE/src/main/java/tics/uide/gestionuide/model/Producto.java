/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad mejorada que representa un producto del menú
 * @author DELL
 */
@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Producto",
        indexes = {
            @Index(name = "idx_producto_nombre", columnList = "nombre"),
            @Index(name = "idx_producto_disponible", columnList = "disponible")
        })
@NamedQueries(value = {
    @NamedQuery(name = "busqueda.ciega",
            query = "SELECT p FROM Producto p WHERE LOWER(p.descripcion) LIKE :buscado OR LOWER(p.nombre) LIKE :buscado")
})
public class Producto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private Double precio;
    
    // Relación con Category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "PRODUCTO_CATEGORY_FK"))
    private Category category;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @Column(length = 255)
    private String imagenUrl;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaCreacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;
    
    // Relaciones
    @JsonIgnore
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<Favoritos> favoritos;
    
    @JsonIgnore
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<DetalleFactura> detallesFactura;
    
    @JsonIgnore
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<DetallePedido> detallesPedido;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
        fechaActualizacion = new Date();
        if (stock == null) {
            stock = 0;
        }
        if (disponible == null) {
            disponible = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
    
    // Getter manual para nombre (por si Lombok falla)
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}