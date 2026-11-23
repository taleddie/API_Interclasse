package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.dao.JogadorDAO;
import com.example.dao.TimeDAO;
import com.example.model.Jogador;
import com.example.util.JsonUtil;

@WebServlet("/jogadores")
public class JogadorController extends HttpServlet {

    private JogadorDAO dao = new JogadorDAO();
    private TimeDAO timeDao = new TimeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            String timeId = request.getParameter("timeId");

            if (timeId != null) {
                JsonUtil.enviar(response, dao.listarPorTime(Integer.parseInt(timeId)));
                return;
            }

            if (id == null) {
                List<Jogador> jogadores = dao.listar();
                JsonUtil.enviar(response, jogadores);
                return;
            }

            Jogador jogador = dao.buscarPorId(Integer.parseInt(id));
            if (jogador == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Jogador nao encontrado");
                return;
            }
            JsonUtil.enviar(response, jogador);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Jogador jogador = JsonUtil.ler(request, Jogador.class);
            if (!validar(jogador, response)) {
                return;
            }
            dao.inserir(jogador);
            JsonUtil.enviar(response, jogador);

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

            Jogador jogador = JsonUtil.ler(request, Jogador.class);
            if (!validar(jogador, response)) {
                return;
            }
            jogador.setId(Integer.parseInt(id));

            if (!dao.atualizar(jogador)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Jogador nao encontrado");
                return;
            }
            JsonUtil.enviar(response, jogador);

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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Jogador nao encontrado");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Jogador removido");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /** valida o corpo do post/put. devolve erro e false se algo estiver errado. */
    private boolean validar(Jogador j, HttpServletResponse response) throws IOException, SQLException {
        if (j == null || j.getNome() == null || j.getNome().isBlank() || j.getTimeId() <= 0) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome e timeId sao obrigatorios");
            return false;
        }
        if (j.getGols() < 0 || j.getAssistencias() < 0) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "gols e assistencias nao podem ser negativos");
            return false;
        }
        if (timeDao.buscarPorId(j.getTimeId()) == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
            return false;
        }
        return true;
    }
}