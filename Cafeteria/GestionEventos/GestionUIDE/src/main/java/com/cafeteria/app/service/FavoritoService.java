package com.cafeteria.app.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.Favoritos;
import com.cafeteria.app.model.Producto;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.FavoritosRepository;

@Service
@Transactional
public class FavoritoService {

    @Autowired
    private FavoritosRepository favoritosRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoService productoService;

    public Favoritos agregar(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Producto producto = productoService.buscarPorId(productoId);

        if (favoritosRepository.existsByUsuarioAndProducto(usuario, producto)) {
            throw new BadRequestException("El producto ya está en favoritos");
        }

        Favoritos favorito = Favoritos.builder()
                .usuario(usuario)
                .producto(producto)
                .build();

        return favoritosRepository.save(favorito);
    }

    public void eliminar(Long id) {
        Favoritos favorito = buscarPorId(id);
        favoritosRepository.delete(favorito);
    }

    public void eliminarPorUsuarioYProducto(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Producto producto = productoService.buscarPorId(productoId);

        Favoritos favorito = favoritosRepository.findByUsuarioAndProducto(usuario, producto)
                .orElseThrow(() -> new NotFoundException("Favorito no encontrado"));

        favoritosRepository.delete(favorito);
    }

    public Favoritos buscarPorId(Long id) {
        return favoritosRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Favorito no encontrado con ID: " + id));
    }

    public List<Favoritos> listarPorUsuario(Long usuarioId) {
        return favoritosRepository.findByUsuario_Id(usuarioId);
    }

    public boolean esFavorito(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Producto producto = productoService.buscarPorId(productoId);
        return favoritosRepository.existsByUsuarioAndProducto(usuario, producto);
    }
}
