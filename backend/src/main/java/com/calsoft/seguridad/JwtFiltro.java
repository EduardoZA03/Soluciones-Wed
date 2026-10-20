package com.calsoft.seguridad;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee la cabecera "Authorization: Bearer &lt;token&gt;" de cada petición.
 * Si el token es válido, registra al usuario y su rol para Spring Security.
 */
@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = req.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            Claims datos = jwtService.validar(cabecera.substring(7));
            if (datos != null) {
                String rol = datos.get("rol", String.class);
                var auth = new UsernamePasswordAuthenticationToken(
                        datos.getSubject(), null, List.of(new SimpleGrantedAuthority(rol)));
                auth.setDetails(datos.get("id", Integer.class));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, resp);
    }
}
