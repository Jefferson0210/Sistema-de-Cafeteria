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
import tics.uide.gestionuide.service.GoogleIdTokenService;
import tics.uide.gestionuide.service.RolService;
import tics.uide.gestionuide.service.UsuarioService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AutenticacionWS {

    @Autowired private UsuarioService usuarioService;
    @Autowired private GoogleIdTokenService googleIdTokenService;
    @Autowired private RolService rolService;
    @Autowired private EmailService emailService;

    // Códigos de recuperación temporales: email -> {codigo, expiracion}
    private final ConcurrentHashMap<String, String[]> codigosRecuperacion = new ConcurrentHashMap<>();

    private String generarToken(Usuario u) {
        long expMs = 1000L * 60 * 60 * 24;
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
            String token = generarToken(usuario);
            // Enviar email de bienvenida (async, no bloquea)
            try { emailService.enviarBienvenida(usuario.getEmail(), usuario.getNombre()); } catch (Exception ignored) {}
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(new ApiResponse(true, "Usuario registrado exitosamente", construirDataUsuario(usuario, token)));
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
        Usuario u = usuarioService.autenticar(loginDto);
        String token = generarToken(u);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new ApiResponse(true, "Login correcto", construirDataUsuario(u, token)));
    }

    // POST /api/auth/recuperar-password — envía código al email
    @PostMapping("/recuperar-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body(new ApiResponse(false, "Email requerido", null));
        try {
            Usuario u = usuarioService.buscarPorEmail(email.trim());
            String codigo = String.format("%06d", new Random().nextInt(999999));
            long expiracion = System.currentTimeMillis() + (15 * 60 * 1000); // 15 min
            codigosRecuperacion.put(email.trim().toLowerCase(), new String[]{codigo, String.valueOf(expiracion)});
            emailService.enviarCodigoRecuperacion(email, u.getNombre(), codigo);
            return ResponseEntity.ok(new ApiResponse(true, "Código enviado a " + email, null));
        } catch (Exception e) {
            // No revelar si el email existe o no (seguridad)
            return ResponseEntity.ok(new ApiResponse(true, "Si el email está registrado, recibirás un código", null));
        }
    }

    // POST /api/auth/verificar-codigo — verifica código y cambia password
    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");
        String nuevaPassword = body.get("nuevaPassword");
        if (email == null || codigo == null || nuevaPassword == null)
            return ResponseEntity.badRequest().body(new ApiResponse(false, "email, codigo y nuevaPassword son requeridos", null));

        String[] datos = codigosRecuperacion.get(email.trim().toLowerCase());
        if (datos == null) return ResponseEntity.badRequest().body(new ApiResponse(false, "No hay código solicitado para este email", null));
        if (System.currentTimeMillis() > Long.parseLong(datos[1])) {
            codigosRecuperacion.remove(email.trim().toLowerCase());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "El código ha expirado. Solicita uno nuevo", null));
        }
        if (!datos[0].equals(codigo.trim())) return ResponseEntity.badRequest().body(new ApiResponse(false, "Código incorrecto", null));

        try {
            Usuario u = usuarioService.buscarPorEmail(email.trim());
            usuarioService.resetPassword(u.getId(), nuevaPassword);
            codigosRecuperacion.remove(email.trim().toLowerCase());
            return ResponseEntity.ok(new ApiResponse(true, "Contraseña actualizada. Ya puedes iniciar sesión", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
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
    public ResponseEntity<?> logout() { return ResponseEntity.ok(new ApiResponse(true, "Logout correcto", null)); }

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
