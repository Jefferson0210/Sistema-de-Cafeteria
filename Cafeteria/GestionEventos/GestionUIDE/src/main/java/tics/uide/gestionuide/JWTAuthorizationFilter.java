package tics.uide.gestionuide;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
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

    // Lee de application.properties via -D o usa fallback
    public static final String KEY_APP = System.getProperty("jwt.secret",
            System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : "CafeteriaUIDE2024SecureKey!@#$%");

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
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
        }
    }

    private Claims validateToken(HttpServletRequest request) {
        String jwtToken = request.getHeader(AUTORIZATION).replace(BEARER, "").trim();
        return Jwts.parser().setSigningKey(KEY_APP.getBytes()).parseClaimsJws(jwtToken).getBody();
    }

    @SuppressWarnings("unchecked")
    private void setUpSpringAuthentication(Claims claims) {
        List<String> authorities = (List<String>) claims.get(PERMISOS);
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
