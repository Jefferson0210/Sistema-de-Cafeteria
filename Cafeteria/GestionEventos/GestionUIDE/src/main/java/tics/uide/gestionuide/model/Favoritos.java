/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Productos favoritos de los usuarios
 * @author DELL
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
@NamedQueries(value = {
    @NamedQuery(name = "favoritos.por.usuario", 
            query = "SELECT f FROM Favoritos f LEFT JOIN FETCH f.producto WHERE f.usuario.id = :usuarioId")
})
public class Favoritos implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ACTUALIZADO: Cambio de Persona a Usuario
    @ManyToOne
    @JoinColumn(name = "usuario", nullable = false, foreignKey = @ForeignKey(name = "FAVORITO_USUARIO_FK"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Usuario usuario; // ← CAMBIO PRINCIPAL
    
    @ManyToOne
    @JoinColumn(name = "producto", nullable = false, foreignKey = @ForeignKey(name = "FAVORITO_PRODUCTO_FK"))
    private Producto producto;
}