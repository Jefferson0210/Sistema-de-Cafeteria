package tics.uide.gestionuide.webservice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tics.uide.gestionuide.dto.ActualizarPerfilDto;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.PageMeta;
import tics.uide.gestionuide.util.PageUtils;
import org.springframework.data.domain.Page;
import tics.uide.gestionuide.dto.UsuarioDto;
import tics.uide.gestionuide.enums.Rol;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.RolService;
import tics.uide.gestionuide.service.UsuarioService;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioWS {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        if (page == null) {
            return ResponseEntity.ok(new ApiResponse(true, "Usuarios",
                usuarioService.listarTodos().stream().map(this::mapUsuario).collect(Collectors.toList())));
        }
        Page<Usuario> p = usuarioService.listarTodos(PageUtils.of(page, size, sort));
        return ResponseEntity.ok(new ApiResponse(true, "Usuarios",
            p.getContent().stream().map(this::mapUsuario).collect(Collectors.toList()), new PageMeta(p)));
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> listarActivos() {
        return ResponseEntity.ok(new ApiResponse(true, "Usuarios activos",
            usuarioService.listarActivos().stream().map(this::mapUsuario).collect(Collectors.toList())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridad.esDuenioOStaff(#id, authentication, 'ADMIN')")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Usuario", mapUsuario(usuarioService.buscarPorId(id))));
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/rol/{rol}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> listarPorRol(@PathVariable Rol rol) {
        return ResponseEntity.ok(new ApiResponse(true, "Usuarios con rol " + rol,
            usuarioService.listarPorRol(rol).stream().map(this::mapUsuario).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioDto dto) {
        try { Rol rol = dto.getRol() != null ? dto.getRol() : Rol.CLIENTE;
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Usuario creado", mapUsuario(usuarioService.crear(dto, rol))));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@seguridad.esDuenioOStaff(#id, authentication, 'ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDto dto) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Usuario actualizado", mapUsuario(usuarioService.actualizar(id, dto))));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> agregarRol(@PathVariable Long id, @RequestParam Rol rol) {
        try { Usuario u = usuarioService.buscarPorId(id); rolService.agregarRol(u, rol);
            return ResponseEntity.ok(new ApiResponse(true, "Rol agregado", mapUsuario(u)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @DeleteMapping("/{id}/rol")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id, @RequestParam Rol rol) {
        try { Usuario u = usuarioService.buscarPorId(id); rolService.eliminarRol(u, rol);
            return ResponseEntity.ok(new ApiResponse(true, "Rol eliminado", mapUsuario(u)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try { usuarioService.desactivar(id); return ResponseEntity.ok(new ApiResponse(true, "Usuario desactivado", null));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> activar(@PathVariable Long id) {
        try { usuarioService.cambiarEstado(id, true); return ResponseEntity.ok(new ApiResponse(true, "Usuario activado", null));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try { usuarioService.eliminar(id); return ResponseEntity.ok(new ApiResponse(true, "Usuario eliminado", null));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("@seguridad.esDuenioOStaff(#id, authentication, 'ADMIN')")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String actual = body.get("passwordActual"), nueva = body.get("passwordNueva");
            if (actual == null || nueva == null) return ResponseEntity.badRequest().body(new ApiResponse(false, "passwordActual y passwordNueva son requeridos", null));
            usuarioService.cambiarPassword(id, actual, nueva);
            return ResponseEntity.ok(new ApiResponse(true, "Contraseña actualizada", null));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    // CORREGIDO: ahora guarda fotoUrl en la BD
    @PostMapping("/{id}/foto")
    @PreAuthorize("@seguridad.esDuenioOStaff(#id, authentication, 'ADMIN')")
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            java.nio.file.Path ruta = java.nio.file.Paths.get("uploads/usuarios").resolve(nombreArchivo);
            java.nio.file.Files.createDirectories(ruta.getParent());
            java.nio.file.Files.copy(archivo.getInputStream(), ruta, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            String url = "/uploads/usuarios/" + nombreArchivo;
            // GUARDAR en BD
            Usuario usuario = usuarioService.buscarPorId(id);
            usuario.setFotoUrl(url);
            usuarioService.guardar(usuario);
            return ResponseEntity.ok(new ApiResponse(true, "Foto actualizada", Map.of("fotoUrl", url)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PutMapping("/{id}/perfil")
    @PreAuthorize("@seguridad.esDuenioOStaff(#id, authentication, 'ADMIN')")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id, @Valid @RequestBody ActualizarPerfilDto dto) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            usuario.setNombre(dto.getNombre());
            usuario.setApellido(dto.getApellido());
            if (!usuario.getEmail().equals(dto.getEmail())) {
                if (!usuarioService.emailDisponible(dto.getEmail())) throw new BadRequestException("El email ya está registrado");
                usuario.setEmail(dto.getEmail());
            }
            usuario.setTelefono(dto.getTelefono());
            usuarioService.guardar(usuario);
            return ResponseEntity.ok(new ApiResponse(true, "Perfil actualizado", mapUsuario(usuario)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    private Map<String, Object> mapUsuario(Usuario u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId()); map.put("username", u.getUsername()); map.put("email", u.getEmail());
        map.put("nombre", u.getNombre()); map.put("apellido", u.getApellido()); map.put("telefono", u.getTelefono());
        map.put("fotoUrl", u.getFotoUrl()); map.put("activo", u.getActivo()); map.put("fechaRegistro", u.getFechaRegistro());
        try { map.put("roles", rolService.obtenerRolesDeUsuario(u).stream().map(r -> r.getRol().name()).collect(Collectors.toList()));
        } catch (Exception e) { map.put("roles", List.of()); }
        return map;
    }
}
