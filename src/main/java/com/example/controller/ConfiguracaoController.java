package com.example.controller;

import com.example.dao.ConfiguracaoDAO;
import com.example.model.Configuracao;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/configuracao")
public class ConfiguracaoController extends HttpServlet {

    private ConfiguracaoDAO dao = new ConfiguracaoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String chave = request.getParameter("chave");

            if (chave == null) {
                JsonUtil.enviar(response, dao.listar());
                return;
            }

            Configuracao config = dao.buscarPorChave(chave);
            if (config == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Configuracao nao encontrada");
                return;
            }
            JsonUtil.enviar(response, config);

        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String chave = request.getParameter("chave");
            if (chave == null || chave.isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "chave e obrigatoria");
                return;
            }

            Configuracao atual = dao.buscarPorChave(chave);
            if (atual == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Configuracao nao encontrada");
                return;
            }

            Configuracao corpo = JsonUtil.ler(request, Configuracao.class);
            if (corpo == null || corpo.getValor() == null || corpo.getValor().isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "valor e obrigatorio");
                return;
            }

            int numero;
            try {
                numero = Integer.parseInt(corpo.getValor().trim());
            } catch (NumberFormatException e) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "valor deve ser um numero inteiro");
                return;
            }
            if (numero < 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "valor nao pode ser negativo");
                return;
            }
            if ("PERMITE_EMPATE".equals(chave) && numero != 0 && numero != 1) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "PERMITE_EMPATE deve ser 0 ou 1");
                return;
            }

            atual.setValor(String.valueOf(numero));
            dao.atualizar(atual);
            JsonUtil.enviar(response, atual);

        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}