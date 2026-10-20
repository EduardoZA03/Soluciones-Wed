package com.calsoft.seguridad;

import com.calsoft.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Crea y valida los tokens JWT de sesión (8 horas por defecto). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${calsoft.jwt.secreto}") String secreto, @Value("${calsoft.jwt.horas}") long horas) {
        if (secreto.length() < 32) {
            throw new IllegalStateException("calsoft.jwt.secreto debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofHours(horas);
    }

    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(u.getUsuario())
                .claim("id", u.getId())
                .claim("rol", u.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    /** Datos del token si es válido y no venció; null si no. */
    public Claims validar(String token) {
        try {
            return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
