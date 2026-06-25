package tics.uide.gestionuide;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    public static final String AUTORIZATION = "Authorization";
    public static final String PERMISOS = "permisos";
    public static final String BEARER = "Bearer";

    // La clave JWT proviene EXCLUSIVAMENTE de -Djwt.secret o de la variable de
    // entorno JWT_SECRET. Sin fallback: si no está definida, la app no arranca.
    public static final String KEY_APP = resolverClaveJwt();

    private static String resolverClaveJwt() {
        String clave = System.getProperty("jwt.secret");
        if (clave == null || clave.trim().isEmpty()) {
            clave = System.getenv("JWT_SECRET");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret no configurado: define JWT_SECRET (o -Djwt.secret) con una " +
                "clave de al menos 256 bits. La aplicación no arranca sin ella.");
        }
        return clave;
    }

    // Fija el algoritmo permitido: solo HS256. Rechaza cualquier otro alg (o "none")
    // a nivel de cabecera, antes de aplicar la clave -> evita algorithm-confusion.
    private static final SigningKeyResolverAdapter KEY_RESOLVER = new SigningKeyResolverAdapter() {
        @Override
        public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
            if (!SignatureAlgorithm.HS256.getValue().equals(header.getAlgorithm())) {
                throw new UnsupportedJwtException("Algoritmo de firma no permitido: " + header.getAlgorithm());
            }
            return KEY_APP.getBytes();
        }
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }
        try {
            if (existeJWTToken(request)) {
                Claims claims = validateToken(request);
                if (claims != null) setUpSpringAuthentication(claims);
                else SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.clearContext();
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
        }
    }

    private Claims validateToken(HttpServletRequest request) {
        String jwtToken = request.getHeader(AUTORIZATION).replace(BEARER, "").trim();
        return Jwts.parser().setSigningKeyResolver(KEY_RESOLVER).parseClaimsJws(jwtToken).getBody();
    }

    private void setUpSpringAuthentication(Claims claims) {
        List<String> authorities = new ArrayList<>();
        Object permisos = claims.get(PERMISOS);
        if (permisos instanceof List<?>) {
            for (Object p : (List<?>) permisos) {
                if (p != null) authorities.add(p.toString());
            }
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private boolean existeJWTToken(HttpServletRequest request) {
        String h = request.getHeader(AUTORIZATION);
        return h != null && h.startsWith(BEARER);
    }
}
