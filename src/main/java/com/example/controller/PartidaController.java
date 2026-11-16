package com.example.controller;

import com.example.dao.PartidaDAO;
import com.example.dao.InscricaoDAO;
import com.example.model.Partida;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/partidas")
public class PartidaController extends HttpServlet {

    private InscricaoDAO inscricaoDao = new InscricaoDAO();
    private PartidaDAO dao = new PartidaDAO();

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
                List<Partida> partidas = dao.listar();
                JsonUtil.enviar(response, partidas);
                return;
            }

            Partida partida = dao.buscarPorId(Integer.parseInt(id));
            if (partida == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                return;
            }
            JsonUtil.enviar(response, partida);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if ("finalizar".equals(request.getParameter("acao"))) {
                String id = request.getParameter("id");
                if (id == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                    return;
                }
                Partida p = dao.buscarPorId(Integer.parseInt(id));
                if (p == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                    return;
                }
                if ("FINALIZADA".equals(p.getStatus())) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida ja esta finalizada");
                    return;
                }
                p.setStatus("FINALIZADA");
                dao.atualizar(p);
                JsonUtil.enviar(response, p);
                return;
            }

            Partida partida = JsonUtil.ler(request, Partida.class);
            if (partida == null || partida.getTimeAId() <= 0 || partida.getTimeBId() <= 0 || partida.getModalidadeId() <= 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "timeAId, timeBId e modalidadeId sao obrigatorios");
                return;
            }
            if (partida.getTimeAId() == partida.getTimeBId()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "Os times devem ser diferentes");
                return;
            }
            if (!inscricaoDao.estaConfirmado(partida.getTimeAId(), partida.getModalidadeId())
                    || !inscricaoDao.estaConfirmado(partida.getTimeBId(), partida.getModalidadeId())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Os dois times precisam estar confirmados na mesma modalidade");
                return;
            }
            partida.setStatus("AGENDADA");
            dao.inserir(partida);
            JsonUtil.enviar(response, partida);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
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

            Partida corpo = JsonUtil.ler(request, Partida.class);
            if (corpo == null || corpo.getPlacarA() < 0 || corpo.getPlacarB() < 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "placarA e placarB sao obrigatorios e nao podem ser negativos");
                return;
            }

            Partida partida = dao.buscarPorId(Integer.parseInt(id));
            if (partida == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                return;
            }

            if ("FINALIZADA".equals(partida.getStatus())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida finalizada nao pode ter o placar alterado");
                return;
            }
            partida.setPlacarA(corpo.getPlacarA());
            partida.setPlacarB(corpo.getPlacarB());

            if (!dao.atualizar(partida)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                return;
            }
            JsonUtil.enviar(response, partida);

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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Partida removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
