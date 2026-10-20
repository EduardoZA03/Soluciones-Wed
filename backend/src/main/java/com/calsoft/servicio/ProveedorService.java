package com.calsoft.servicio;

import static com.calsoft.servicio.CategoriaService.vacioANull;

import com.calsoft.error.NoEncontradoException;
import com.calsoft.error.ValidacionException;
import com.calsoft.modelo.Proveedor;
import com.calsoft.repositorio.ProveedorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de la tabla maestra Proveedores. */
@Service
public class ProveedorService {

    private final ProveedorRepository repo;

    public ProveedorService(ProveedorRepository repo) {
        this.repo = repo;
    }

    public List<Proveedor> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    public Proveedor buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("Proveedor no encontrado"));
    }

    @Transactional
    public Proveedor crear(Proveedor datos) {
        Proveedor p = new Proveedor();
        copiar(datos, p);
        p.setApiKey(vacioANull(datos.getApiKey()));
        return repo.save(p);
    }

    /** Si la API key llega vacía al editar, se conserva la anterior. */
    @Transactional
    public Proveedor actualizar(Integer id, Proveedor datos) {
        Proveedor p = buscar(id);
        copiar(datos, p);
        if (datos.getApiKey() != null && !datos.getApiKey().isBlank()) {
            p.setApiKey(datos.getApiKey().trim());
        }
        return p;
    }

    @Transactional
    public void eliminar(Integer id) {
        Proveedor p = buscar(id);
        if (repo.contarOrdenes(id) > 0) {
            throw new ValidacionException("No se puede eliminar \"" + p.getNombre()
                    + "\": tiene órdenes de compra registradas");
        }
        repo.delete(p); // los insumos que lo tenían quedan sin proveedor (ON DELETE SET NULL)
    }

    private static void copiar(Proveedor origen, Proveedor destino) {
        destino.setNombre(origen.getNombre().trim());
        destino.setRuc(vacioANull(origen.getRuc()));
        destino.setTelefono(vacioANull(origen.getTelefono()));
        destino.setEmail(vacioANull(origen.getEmail()));
        destino.setApiUrl(vacioANull(origen.getApiUrl()));
    }
}
