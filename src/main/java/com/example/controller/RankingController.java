package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.dao.JogadorDAO;
import com.example.util.JsonUtil;

@WebServlet("/ranking")
public class RankingController extends HttpServlet {

    private JogadorDAO dao = new JogadorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String tipo = request.getParameter("tipo");
            if (tipo == null) {
                tipo = "gols";
            }

            if ("gols".equals(tipo)) {
                JsonUtil.enviar(response, dao.rankingGols());
            } else if ("assistencias".equals(tipo)) {
                JsonUtil.enviar(response, dao.rankingAssistencias());
            } else {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "tipo deve ser gols ou assistencias");
            }

        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}