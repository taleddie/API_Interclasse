package com.example.controller;

import com.example.dao.ModalidadeDAO;
import com.example.model.Modalidade;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/modalidades")
public class ModalidadeController extends HttpServlet {

    private ModalidadeDAO dao = new ModalidadeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                List<Modalidade> modalidades = dao.listar();
                JsonUtil.enviar(response, modalidades);
                return;
            }

            Modalidade modalidade = dao.buscarPorId(Integer.parseInt(id));
            if (modalidade == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Modalidade nao encontrado");
                return;
            }
            JsonUtil.enviar(response, modalidade);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Modalidade modalidade = JsonUtil.ler(request, Modalidade.class);
            if (modalidade == null || modalidade.getNome() == null || modalidade.getNome().isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }
            dao.inserir(modalidade);
            JsonUtil.enviar(response, modalidade);

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

            Modalidade modalidade = JsonUtil.ler(request, Modalidade.class);
            if (modalidade == null || modalidade.getNome() == null || modalidade.getNome().isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }
            if (modalidade.getAtivo() != 0 && modalidade.getAtivo() != 1) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "ativo deve ser 0 ou 1");
                return;
            }
            modalidade.setId(Integer.parseInt(id));

            if (!dao.atualizar(modalidade)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Modalidade nao encontrado");
                return;
            }
            JsonUtil.enviar(response, modalidade);

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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Modalidade nao encontrado");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Modalidade removido");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
