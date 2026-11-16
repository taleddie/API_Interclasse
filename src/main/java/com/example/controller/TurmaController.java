package com.example.controller;

import com.example.dao.TurmaDAO;
import com.example.model.Turma;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/turmas")
public class TurmaController extends HttpServlet {

    private TurmaDAO dao = new TurmaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                List<Turma> turmas = dao.listar();
                JsonUtil.enviar(response, turmas);
                return;
            }

            Turma turma = dao.buscarPorId(Integer.parseInt(id));
            if (turma == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrado");
                return;
            }
            JsonUtil.enviar(response, turma);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Turma turma = JsonUtil.ler(request, Turma.class);
            if (turma == null || turma.getNome() == null || turma.getNome().isBlank() || turma.getCurso() == null || turma.getCurso().isBlank() || turma.getAno() <= 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome, curso e ano sao obrigatorio");
                return;
            }
            dao.inserir(turma);
            JsonUtil.enviar(response, turma);

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

            Turma turma = JsonUtil.ler(request, Turma.class);
            if (turma == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }
            turma.setId(Integer.parseInt(id));

            if (!dao.atualizar(turma)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrado");
                return;
            }
            JsonUtil.enviar(response, turma);

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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrado");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Turma removido");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
