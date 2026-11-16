package com.example.controller;

import com.example.dao.InscricaoDAO;
import com.example.model.Inscricao;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/inscricoes")
public class InscricaoController extends HttpServlet {

    private InscricaoDAO dao = new InscricaoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            String modalidadeId = request.getParameter("modalidadeId");

            if (modalidadeId != null) {
                JsonUtil.enviar(response, dao.listarPorModalidade(Integer.parseInt(modalidadeId)));
                return;
            }

            if (id == null) {
                List<Inscricao> inscricoes = dao.listar();
                JsonUtil.enviar(response, inscricoes);
                return;
            }

            Inscricao inscricao = dao.buscarPorId(Integer.parseInt(id));
            if (inscricao == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }
            JsonUtil.enviar(response, inscricao);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Inscricao inscricao = JsonUtil.ler(request, Inscricao.class);
            if (inscricao == null || inscricao.getTimeId() <= 0 || inscricao.getModalidadeId() <= 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "timeId e modalidadeId sao obrigatorios");
                return;
            }
            if (dao.existeInscricao(inscricao.getTimeId())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Time ja esta inscrito em uma modalidade");
                return;
            }
            inscricao.setStatus("PENDENTE");
            dao.inserir(inscricao);
            JsonUtil.enviar(response, inscricao);

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

            Inscricao corpo = JsonUtil.ler(request, Inscricao.class);
            if (corpo == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }
            String status = corpo.getStatus();
            if (!"CONFIRMADA".equals(status) && !"NEGADA".equals(status)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "status deve ser CONFIRMADA ou NEGADA");
                return;
            }

            Inscricao inscricao = dao.buscarPorId(Integer.parseInt(id));
            if (inscricao == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }
            inscricao.setStatus(status);

            if (!dao.atualizar(inscricao)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }
            JsonUtil.enviar(response, inscricao);

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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Inscricao removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
