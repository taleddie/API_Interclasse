package com.example.controller;

import com.example.dao.TimeDAO;
import com.example.model.Time;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller de exemplo com as operacoes basicas (CRUD).
 *
 * Rotas:
 *   GET    /api/times      -> lista todos os times
 *   GET    /api/times?id=1 -> busca um time
 *   POST   /api/times      -> cria um time
 *   PUT    /api/times?id=1 -> atualiza um time
 *   DELETE /api/times?id=1 -> remove um time
 */
@WebServlet("/times")
public class TimeController extends HttpServlet {

    private TimeDAO dao = new TimeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                List<Time> times = dao.listar();
                JsonUtil.enviar(response, times);
                return;
            }

            Time time = dao.buscarPorId(Integer.parseInt(id));
            if (time == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                return;
            }
            JsonUtil.enviar(response, time);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Time time = JsonUtil.ler(request, Time.class);
            if (time == null || time.getNome() == null || time.getNome().isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }
            dao.inserir(time);
            JsonUtil.enviar(response, time);

        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }

            Time time = JsonUtil.ler(request, Time.class);
            if (time == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }
            time.setId(Integer.parseInt(id));

            if (!dao.atualizar(time)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                return;
            }
            JsonUtil.enviar(response, time);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }

            if (!dao.deletar(Integer.parseInt(id))) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Time removido");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
