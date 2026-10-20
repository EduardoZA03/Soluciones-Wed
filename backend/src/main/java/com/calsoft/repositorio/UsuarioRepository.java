package com.calsoft.repositorio;

import com.calsoft.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    boolean existsByRol(String rol);

    List<Usuario> findAllByOrderByUsuarioAsc();
}
