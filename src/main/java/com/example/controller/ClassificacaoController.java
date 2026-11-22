package com.example.controller;

import com.example.dao.ConfiguracaoDAO;
import com.example.dao.InscricaoDAO;
import com.example.dao.PartidaDAO;
import com.example.dao.TimeDao;
import com.example.model.ClassificacaoLinha;
import com.example.model.Inscricao;
import com.example.model.Partida;
import com.example.model.Time;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/classificacao")
public class ClassificacaoController extends HttpServlet {

    private PartidaDAO partidaDao = new PartidaDAO();
    private InscricaoDAO inscricaoDao = new InscricaoDAO();
    private TimeDao timeDao = new TimeDao();
    private ConfiguracaoDAO configDao = new ConfiguracaoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String param = request.getParameter("modalidadeId");
            if (param == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "modalidadeId e obrigatorio");
                return;
            }
            int modalidadeId = Integer.parseInt(param);

            int pontosVitoria = configDao.buscarInt("PONTOS_VITORIA", 3);
            int pontosEmpate = configDao.buscarInt("PONTOS_EMPATE", 1);
            int pontosDerrota = configDao.buscarInt("PONTOS_DERROTA", 0);

            Map<Integer, ClassificacaoLinha> tabela = new LinkedHashMap<>();

            for (Inscricao i : inscricaoDao.listarPorModalidade(modalidadeId)) {
                if ("CONFIRMADA".equals(i.getStatus())) {
                    linhaDoTime(tabela, i.getTimeId());
                }
            }

            for (Partida p : partidaDao.listarPorModalidade(modalidadeId)) {
                if (!"FINALIZADA".equals(p.getStatus())) {
                    continue;
                }
                int golsA = p.getPlacarA() == null ? 0 : p.getPlacarA();
                int golsB = p.getPlacarB() == null ? 0 : p.getPlacarB();

                linhaDoTime(tabela, p.getTimeAId()).registrarJogo(golsA, golsB, pontosVitoria, pontosEmpate, pontosDerrota);
                linhaDoTime(tabela, p.getTimeBId()).registrarJogo(golsB, golsA, pontosVitoria, pontosEmpate, pontosDerrota);
            }

            List<ClassificacaoLinha> lista = new ArrayList<>(tabela.values());
            lista.sort(Comparator.comparingInt(ClassificacaoLinha::getPontos).reversed()
                    .thenComparing(Comparator.comparingInt(ClassificacaoLinha::getVitorias).reversed())
                    .thenComparing(Comparator.comparingInt(ClassificacaoLinha::getSaldo).reversed())
                    .thenComparing(ClassificacaoLinha::getTime, String.CASE_INSENSITIVE_ORDER));

            JsonUtil.enviar(response, lista);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "modalidadeId invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ClassificacaoLinha linhaDoTime(Map<Integer, ClassificacaoLinha> tabela, int timeId) throws SQLException {
        ClassificacaoLinha linha = tabela.get(timeId);
        if (linha == null) {
            Time time = timeDao.buscarPorId(timeId);
            String nome = (time != null) ? time.getNome() : "Time " + timeId;
            linha = new ClassificacaoLinha(timeId, nome);
            tabela.put(timeId, linha);
        }
        return linha;
    }
}