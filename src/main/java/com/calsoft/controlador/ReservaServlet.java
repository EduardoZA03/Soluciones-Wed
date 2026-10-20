package com.calsoft.controlador;

import com.calsoft.dao.ReservaDAO;
import com.calsoft.modelo.Reserva;
import com.calsoft.util.Formulario;
import com.calsoft.util.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reservas con fecha/hora, personas y estado. */
@WebServlet("/reservas")
public class ReservaServlet extends ServletBase {

    private static final long serialVersionUID = 1L;

    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Reserva> reservas = new ArrayList<>();
        try {
            reservas = reservaDAO.listar();
        } catch (SQLException e) {
            errorServidor(req, "LISTAR RESERVAS", e);
        }

        int pendientes = 0;
        int confirmadas = 0;
        int canceladas = 0;
        for (Reserva r : reservas) {
            switch (r.getEstado()) {
                case "Confirmada":
                    confirmadas++;
                    break;
                case "Cancelada":
                    canceladas++;
                    break;
                default:
                    pendientes++;
            }
        }

        req.setAttribute("reservas", reservas);
        req.setAttribute("total", reservas.size());
        req.setAttribute("pendientes", pendientes);
        req.setAttribute("confirmadas", confirmadas);
        req.setAttribute("canceladas", canceladas);
        req.setAttribute("estados", Reserva.ESTADOS);
        mostrar(req, resp, "reservas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Formulario f = new Formulario(req);
        String accion = f.texto("accion");
        try {
            switch (accion) {
                case "crear":
                    crear(f);
                    exito(req, "Reserva registrada");
                    break;
                case "estado":
                    String estado = f.texto("estado");
                    if (!Reserva.ESTADOS.contains(estado)) {
                        throw new ValidacionException("Estado inválido");
                    }
                    if (!reservaDAO.actualizarEstado(f.id(), estado)) {
                        error(req, "Reserva no encontrada");
                    }
                    break;
                case "eliminar":
                    if (reservaDAO.eliminar(f.id())) {
                        exito(req, "Reserva eliminada");
                    } else {
                        error(req, "Reserva no encontrada");
                    }
                    break;
                default:
                    error(req, "Acción no válida");
            }
        } catch (ValidacionException e) {
            error(req, e.getMessage());
        } catch (SQLException e) {
            errorServidor(req, "RESERVAS " + accion, e);
        }
        redirigir(req, resp, "/reservas");
    }

    private void crear(Formulario f) throws ValidacionException, SQLException {
        Reserva r = new Reserva();
        r.setCliente(f.texto("cliente"));
        if (r.getCliente().isEmpty() || f.texto("fecha").isEmpty() || f.texto("hora").isEmpty()) {
            throw new ValidacionException("Cliente, fecha y hora son obligatorios");
        }
        String telefono = f.texto("telefono");
        r.setTelefono(telefono.isEmpty() ? null : telefono);
        r.setFecha(f.fecha("fecha", "Fecha inválida"));
        r.setHora(f.hora("hora", "Hora inválida"));
        r.setPersonas(f.entero("personas", 1, "Personas debe ser un entero positivo"));
        r.setMesa(f.entero("mesa", 1, "La mesa debe ser un entero positivo"));
        String estado = f.texto("estado", "Pendiente");
        if (!Reserva.ESTADOS.contains(estado)) {
            throw new ValidacionException("Estado inválido");
        }
        r.setEstado(estado);
        reservaDAO.crear(r);
    }
}
