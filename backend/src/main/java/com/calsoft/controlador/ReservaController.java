package com.calsoft.controlador;

import com.calsoft.dto.Dtos.EstadoRequest;
import com.calsoft.dto.Dtos.ReservaRequest;
import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Reserva;
import com.calsoft.repositorio.ReservaRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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

/** Reservas de mesa. */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaRepository repo;

    public ReservaController(ReservaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Reserva> listar() {
        return repo.findAllByOrderByFechaAscHoraAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@Valid @RequestBody ReservaRequest r) {
        String estado = r.estado() == null || r.estado().isBlank() ? "Pendiente" : r.estado();
        if (!Reserva.ESTADOS.contains(estado)) {
            throw new ValidacionException("Estado inválido");
        }
        Reserva res = new Reserva();
        res.setCliente(r.cliente().trim());
        res.setTelefono(r.telefono() == null || r.telefono().isBlank() ? null : r.telefono().trim());
        res.setFecha(r.fecha());
        res.setHora(r.hora());
        res.setPersonas(r.personas());
        res.setMesa(r.mesa());
        res.setEstado(estado);
        return repo.save(res);
    }

    @PatchMapping("/{id}/estado")
    @Transactional
    public Reserva cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest r) {
        if (!Reserva.ESTADOS.contains(r.estado())) {
            throw new ValidacionException("Estado inválido");
        }
        Reserva res = repo.findById(id).orElseThrow(() -> new NoEncontradoException("Reserva no encontrada"));
        res.setEstado(r.estado());
        return res;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        if (!repo.existsById(id)) {
            throw new NoEncontradoException("Reserva no encontrada");
        }
        repo.deleteById(id);
    }
}
