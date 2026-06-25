package tics.uide.gestionuide.webservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.LoginDto;
import tics.uide.gestionuide.dto.OAuth2LoginDto;
import tics.uide.gestionuide.dto.UsuarioRegistroDto;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.EmailService;
import tics.uide.gestionuide.service.EmailVerificationService;
import tics.uide.gestionuide.service.GoogleIdTokenService;
import tics.uide.gestionuide.service.PasswordResetService;
import tics.uide.gestionuide.service.RefreshTokenService;
import tics.uide.gestionuide.service.RateLimitService;
import tics.uide.gestionuide.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import tics.uide.gestionuide.service.RolService;
import tics.uide.gestionuide.service.UsuarioService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionWS {

    @Autowired private UsuarioService usuarioService;
    @Autowired private GoogleIdTokenService googleIdTokenService;
    @Autowired private RolService rolService;
    @Autowired private EmailService emailService;
    @Autowired private PasswordResetService passwordResetService;
    @Autowired private EmailVerificationService emailVerificationService;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private RateLimitService rateLimitService;

    @Value("${app.jwt.access-ttl-minutes:15}")
    private long accessTtlMinutes;

    private String generarToken(Usuario u) {
        long expMs = accessTtlMinutes * 60 * 1000L;   // access token corto (configurable)
        return Jwts.builder()
                .setSubject(u.getUsername())
                .claim(tics.uide.gestionuide.JWTAuthorizationFilter.PERMISOS, extraerRoles(u))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(SignatureAlgorithm.HS256, tics.uide.gestionuide.JWTAuthorizationFilter.KEY_APP.getBytes())
                .compact();
    }

    // POST /api/auth/registro — con email de bienvenida
    @PostMapping(value = "/registro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registro(@Valid @RequestBody UsuarioRegistroDto registroDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Errores de validación", errores));
        }
        try {
            Usuario usuario = usuarioService.registrar(registroDto);
            // Doble opt-in: se envía verificación y NO se auto-loguea (no se devuelve JWT).
            emailVerificationService.enviarVerificacion(usuario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true,
                            "Registrado. Revisa tu correo para verificar tu cuenta.",
                            construirDataUsuario(usuario, null)));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/auth/login
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        if (loginDto == null) throw new BadRequestException("Body requerido");
        if (loginDto.getUsernameOrEmail() == null || loginDto.getUsernameOrEmail().trim().isEmpty()) throw new BadRequestException("usernameOrEmail requerido");
        if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) throw new BadRequestException("password requerido");
        long retryEmail = rateLimitService.chequearEmail("login", loginDto.getUsernameOrEmail());
        if (retryEmail > 0) throw new TooManyRequestsException(retryEmail);
        Usuario u = usuarioService.autenticar(loginDto);
        String access = generarToken(u);
        String refresh = refreshTokenService.emitir(u);
        Map<String, Object> data = construirDataUsuario(u, access);
        data.put("refreshToken", refresh);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                .body(new ApiResponse(true, "Login correcto", data));
    }

    // POST /api/auth/refresh — { refreshToken } -> nuevo access + rota el refresh
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        try {
            RefreshTokenService.Rotacion r = refreshTokenService.rotar(body.get("refreshToken"));
            String access = generarToken(r.usuario);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", access);
            data.put("tokenType", "Bearer");
            data.put("refreshToken", r.nuevoRefresh);
            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                    .body(new ApiResponse(true, "Token renovado", data));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/auth/recuperar-password — solicita el reset; responde SIEMPRE igual (no filtra si el email existe)
    @PostMapping("/recuperar-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        long retry = rateLimitService.chequearEmail("recuperar", email);
        if (retry > 0) throw new TooManyRequestsException(retry);
        passwordResetService.solicitar(email);
        return ResponseEntity.ok(new ApiResponse(true,
                "Si el email está registrado, recibirás un enlace para restablecer tu contraseña.", null));
    }

    // POST /api/auth/restablecer-password — confirma el reset con { token, nuevaPassword }
    @PostMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String nuevaPassword = body.get("nuevaPassword");
        if (token == null || token.isBlank() || nuevaPassword == null || nuevaPassword.length() < 8) {
            return ResponseEntity.badRequest().body(new ApiResponse(false,
                    "token y nuevaPassword (mínimo 8 caracteres) son requeridos", null));
        }
        try {
            passwordResetService.restablecer(token, nuevaPassword);
            return ResponseEntity.ok(new ApiResponse(true, "Contraseña actualizada. Ya puedes iniciar sesión", null));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/auth/verificar-email — confirma el correo con { token }
    @PostMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(new ApiResponse(false, "token requerido", null));
        try {
            emailVerificationService.verificar(token);
            return ResponseEntity.ok(new ApiResponse(true, "Correo verificado. Ya puedes iniciar sesión.", null));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/auth/reenviar-verificacion — reenvía el enlace; respuesta genérica (no enumera)
    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<?> reenviarVerificacion(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        long retry = rateLimitService.chequearEmail("reenviar", email);
        if (retry > 0) throw new TooManyRequestsException(retry);
        emailVerificationService.reenviar(email);
        return ResponseEntity.ok(new ApiResponse(true,
                "Si el email está registrado y pendiente de verificar, recibirás un nuevo enlace.", null));
    }

    @GetMapping("/verificar/username/{username}")
    public ResponseEntity<?> verificarUsername(@PathVariable String username) {
        boolean d = usuarioService.usernameDisponible(username.trim());
        return ResponseEntity.ok(new ApiResponse(true, d ? "Disponible" : "En uso", Map.of("disponible", d)));
    }

    @GetMapping("/verificar/email")
    public ResponseEntity<?> verificarEmail(@RequestParam String email) {
        boolean d = usuarioService.emailDisponible(email.trim());
        return ResponseEntity.ok(new ApiResponse(true, d ? "Disponible" : "Registrado", Map.of("disponible", d)));
    }

    @PostMapping(value = "/oauth2/google", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody OAuth2LoginDto oauth2Dto) {
        try {
            var payload = googleIdTokenService.verificar(oauth2Dto.getIdToken());
            String email = payload.getEmail();
            String nombre = (String) payload.get("given_name");
            String apellido = (String) payload.get("family_name");
            String picture = (String) payload.get("picture");
            if (nombre == null || nombre.trim().isEmpty()) nombre = (String) payload.get("name");
            if (apellido == null) apellido = "";
            OAuth2LoginDto safeDto = OAuth2LoginDto.builder()
                    .email(email).nombre(nombre != null ? nombre : "Usuario")
                    .apellido(apellido).provider("google").idToken(oauth2Dto.getIdToken()).build();
            Usuario usuario = usuarioService.autenticarORegistrarOAuth2(safeDto);
            // Guardar foto de Google si no tiene foto propia
            if (picture != null && (usuario.getFotoUrl() == null || usuario.getFotoUrl().isEmpty())) {
                usuario.setFotoUrl(picture);
                usuarioService.guardar(usuario);
            }
            String token = generarToken(usuario);
            Map<String, Object> data = construirDataUsuario(usuario, token);
            data.put("provider", "google");
            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(new ApiResponse(true, "Login con Google exitoso", data));
        } catch (BadRequestException e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "No autenticado", null));
        try { Usuario u = usuarioService.buscarPorUsername(principal.getName());
            return ResponseEntity.ok(new ApiResponse(true, "OK", construirDataUsuario(u, null)));
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Token inválido", null)); }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> body) {
        if (body != null) refreshTokenService.revocar(body.get("refreshToken"));
        return ResponseEntity.ok(new ApiResponse(true, "Logout correcto", null));
    }

    private Map<String, Object> construirDataUsuario(Usuario u, String token) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", u.getId()); data.put("username", u.getUsername()); data.put("email", u.getEmail());
        data.put("nombre", u.getNombre()); data.put("apellido", u.getApellido()); data.put("telefono", u.getTelefono());
        data.put("fotoUrl", u.getFotoUrl()); data.put("roles", extraerRoles(u));
        if (token != null) { data.put("token", token); data.put("tokenType", "Bearer"); }
        return data;
    }

    private List<String> extraerRoles(Usuario u) {
        try { return rolService.obtenerRolesDeUsuario(u).stream().map(r -> r.getRol().name()).distinct().collect(Collectors.toList());
        } catch (Exception e) { return List.of(); }
    }
}
