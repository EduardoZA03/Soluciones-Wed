package com.calsoft.controlador;

import com.calsoft.dto.Dtos.LoginRequest;
import com.calsoft.dto.Dtos.LoginResponse;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.modelo.Usuario;
import com.calsoft.repositorio.UsuarioRepository;
import com.calsoft.seguridad.JwtService;
import com.calsoft.seguridad.Roles;
import com.calsoft.seguridad.UsuarioActual;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inicio de sesión (HU-01): valida usuario y clave (BCrypt) y devuelve un token JWT. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UsuarioRepository usuarioRepo, PasswordEncoder encoder, JwtService jwt) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r) {
        Usuario u = usuarioRepo.findByUsuario(r.usuario().trim()).orElse(null);
        // Mismo mensaje si el usuario no existe o la clave no coincide.
        if (u == null || !encoder.matches(r.clave().trim(), u.getClave())) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario o contraseña incorrectos"));
        }
        return ResponseEntity.ok(new LoginResponse(jwt.generar(u), u.getId(), u.getUsuario(), u.getRol(),
                Roles.modulosDe(u.getRol())));
    }

    /** Datos actualizados del usuario conectado (rol y módulos). */
    @GetMapping("/yo")
    public LoginResponse yo() {
        Usuario u = usuarioRepo.findById(UsuarioActual.id())
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
        return new LoginResponse(null, u.getId(), u.getUsuario(), u.getRol(), Roles.modulosDe(u.getRol()));
    }
}
