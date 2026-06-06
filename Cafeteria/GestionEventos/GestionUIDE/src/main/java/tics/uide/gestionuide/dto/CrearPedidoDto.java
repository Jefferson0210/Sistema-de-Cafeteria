package tics.uide.gestionuide.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonAlias({"idMesa", "mesaId"})
    private Long mesaId;

    @JsonAlias({"idCliente", "clienteId"})
    private Long clienteId;

    private Long meseroId; // Opcional: se auto-asigna si no viene

    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto")
    @Valid
    private List<ItemPedidoDto> items;
}