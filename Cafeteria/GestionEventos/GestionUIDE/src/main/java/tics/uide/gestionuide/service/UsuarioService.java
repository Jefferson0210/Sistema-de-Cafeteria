package tics.uide.gestionuide.service;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.LoginDto;
import tics.uide.gestionuide.dto.OAuth2LoginDto;
import tics.uide.gestionuide.dto.UsuarioDto;
import tics.uide.gestionuide.dto.UsuarioRegistroDto;
import tics.uide.gestionuide.enums.Rol;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolService rolService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));
    }

    /** Búsqueda por email que NO lanza (Optional). Útil para no marcar rollback-only en flujos silenciosos. */
    public java.util.Optional<Usuario> buscarPorEmailOpcional(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario buscarPorUsernameOEmail(String usernameOrEmail) {
        return usuarioRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usernameOrEmail));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public org.springframework.data.domain.Page<Usuario> listarTodos(org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRoles_Rol(rol);
    }

    public Usuario registrar(UsuarioRegistroDto registroDto) {
        if (!registroDto.getPassword().equals(registroDto.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        if (usuarioRepository.existsByUsername(registroDto.getUsername())) {
            throw new BadRequestException("El username ya está en uso");
        }

        if (usuarioRepository.existsByEmail(registroDto.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .username(registroDto.getUsername())
                .email(registroDto.getEmail())
                .password(encriptarPassword(registroDto.getPassword()))
                .nombre(registroDto.getNombre())
                .apellido(registroDto.getApellido())
                .telefono(registroDto.getTelefono())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        rolService.agregarRol(usuario, Rol.CLIENTE);

        return usuario;
    }

    public Usuario crear(UsuarioDto usuarioDto, Rol rol) {
        if (usuarioRepository.existsByUsername(usuarioDto.getUsername())) {
            throw new BadRequestException("El username ya está en uso");
        }

        if (usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .username(usuarioDto.getUsername())
                .email(usuarioDto.getEmail())
                .password(encriptarPassword(usuarioDto.getPassword()))
                .nombre(usuarioDto.getNombre())
                .apellido(usuarioDto.getApellido())
                .telefono(usuarioDto.getTelefono())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        rolService.agregarRol(usuario, rol);

        return usuario;
    }
    

    public Usuario autenticar(LoginDto loginDto) {
        Usuario usuario = buscarPorUsernameOEmail(loginDto.getUsernameOrEmail());

        if (!verificarPassword(loginDto.getPassword(), usuario.getPassword())) {
            throw new BadRequestException("Contraseña incorrecta");
        }

        if (!usuario.getActivo()) {
            throw new BadRequestException("Usuario inactivo");
        }

        if (!Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new BadRequestException("Debes verificar tu correo antes de iniciar sesión.");
        }

        return usuario;
    }
    public void resetPassword(Long id, String nuevaPassword) {
    Usuario usuario = buscarPorId(id);
    usuario.setPassword(encriptarPassword(nuevaPassword));
    usuarioRepository.save(usuario);
}

    /**
     * Autenticar o registrar usuario mediante OAuth2 (Google/Microsoft)
     * Si el usuario existe, lo retorna; si no, lo crea
     */
    public Usuario autenticarORegistrarOAuth2(OAuth2LoginDto oauth2Dto) {
        String email = oauth2Dto.getEmail();
        
        // Verificar si el usuario ya existe
        try {
            Usuario usuarioExistente = buscarPorEmail(email);
            
            // Verificar que el usuario esté activo
            if (!usuarioExistente.getActivo()) {
                throw new BadRequestException("Usuario inactivo");
            }
            
            return usuarioExistente;
            
        } catch (NotFoundException e) {
            // Usuario no existe, crear uno nuevo
            return crearUsuarioOAuth2(oauth2Dto);
        }
    }

    /**
     * Crear nuevo usuario desde OAuth2
     */
    private Usuario crearUsuarioOAuth2(OAuth2LoginDto oauth2Dto) {
        // Generar username único basado en el email
        String baseUsername = oauth2Dto.getEmail().split("@")[0];
        String username = generarUsernameUnico(baseUsername);

        // Crear usuario con password aleatoria (no se usará para login)
        Usuario usuario = Usuario.builder()
                .username(username)
                .email(oauth2Dto.getEmail())
                .password(encriptarPassword(UUID.randomUUID().toString()))
                .nombre(oauth2Dto.getNombre() != null ? oauth2Dto.getNombre() : "Usuario")
                .apellido(oauth2Dto.getApellido() != null ? oauth2Dto.getApellido() : "OAuth2")
                .telefono(null)
                .activo(true)
                .emailVerificado(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        rolService.agregarRol(usuario, Rol.CLIENTE);

        return usuario;
    }

    /**
     * Generar username único
     */
    private String generarUsernameUnico(String baseUsername) {
        String username = baseUsername;
        int contador = 1;

        while (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + contador;
            contador++;
        }

        return username;
    }

    public Usuario actualizar(Long id, UsuarioDto usuarioDto) {
        Usuario usuario = buscarPorId(id);

        if (!usuario.getUsername().equals(usuarioDto.getUsername())) {
            if (usuarioRepository.existsByUsername(usuarioDto.getUsername())) {
                throw new BadRequestException("El username ya está en uso");
            }
            usuario.setUsername(usuarioDto.getUsername());
        }

        if (!usuario.getEmail().equals(usuarioDto.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
                throw new BadRequestException("El email ya está registrado");
            }
            usuario.setEmail(usuarioDto.getEmail());
        }

        usuario.setNombre(usuarioDto.getNombre());
        usuario.setApellido(usuarioDto.getApellido());
        usuario.setTelefono(usuarioDto.getTelefono());

        return usuarioRepository.save(usuario);
    }

    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = buscarPorId(id);

        if (!verificarPassword(passwordActual, usuario.getPassword())) {
            throw new BadRequestException("Contraseña actual incorrecta");
        }

        usuario.setPassword(encriptarPassword(passwordNueva));
        usuarioRepository.save(usuario);
        // Seguridad: al cambiar la contraseña, se revocan todas las sesiones (refresh tokens) del usuario.
        refreshTokenService.revocarTodos(id);
    }

    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    public void desactivar(Long id) {
        cambiarEstado(id, false);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
        auditService.registrar("USUARIO_ELIMINADO", "Usuario", id, "username=" + usuario.getUsername());
    }

    public boolean usernameDisponible(String username) {
        return !usuarioRepository.existsByUsername(username);
    }

    public boolean emailDisponible(String email) {
        return !usuarioRepository.existsByEmail(email);
    }

    private String encriptarPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verificarPassword(String password, String passwordEncriptada) {
        try {
            return BCrypt.checkpw(password, passwordEncriptada);
        } catch (Exception e) {
            return false;
        }
    }
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /** Marca el email como verificado cargando el usuario managed (evita problemas de entity detached). */
    public void marcarEmailVerificado(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }
}