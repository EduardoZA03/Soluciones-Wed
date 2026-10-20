package com.calsoft.controlador;

import com.calsoft.dto.Dtos.UsuarioCambio;
import com.calsoft.dto.Dtos.UsuarioRequest;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Usuario;
import com.calsoft.repositorio.UsuarioRepository;
import com.calsoft.seguridad.Roles;
import com.calsoft.seguridad.UsuarioActual;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Usuarios y roles (HU-01). La clave se guarda con BCrypt. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping
    public List<Usuario> listar() {
        return repo.findAllByOrderByUsuarioAsc();
    }

    @GetMapping("/roles")
    public List<String> roles() {
        return Roles.TODOS;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody UsuarioRequest r) {
        String rol = r.rol() == null || r.rol().isBlank() ? Roles.EMPLEADO : r.rol();
        validarRol(rol);
        if (repo.existsByUsuario(r.usuario().trim())) {
            throw new ValidacionException("Ese nombre de usuario ya existe");
        }
        Usuario u = new Usuario();
        u.setUsuario(r.usuario().trim());
        u.setClave(encoder.encode(r.clave().trim()));
        u.setRol(rol);
        return repo.save(u);
    }

    /** Cambia el rol y/o resetea la clave. */
    @PatchMapping("/{id}")
    @Transactional
    public Usuario actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioCambio r) {
        Usuario u = repo.findById(id).orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
        if (r.rol() == null && r.clave() == null) {
            throw new ValidacionException("Nada que actualizar");
        }
        if (r.rol() != null) {
            validarRol(r.rol());
            if (id.equals(UsuarioActual.id()) && !Roles.ADMIN.equals(r.rol())) {
                throw new ValidacionException("No puedes quitarte tu propio rol de Administrador");
            }
            u.setRol(r.rol());
        }
        if (r.clave() != null) {
            u.setClave(encoder.encode(r.clave().trim()));
        }
        return u;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        if (id.equals(UsuarioActual.id())) {
            throw new ValidacionException("No puedes eliminar tu propia cuenta");
        }
        if (!repo.existsById(id)) {
            throw new NoEncontradoException("Usuario no encontrado");
        }
        repo.deleteById(id);
    }

    private static void validarRol(String rol) {
        if (!Roles.TODOS.contains(rol)) {
            throw new ValidacionException("Rol inválido");
        }
    }
}
