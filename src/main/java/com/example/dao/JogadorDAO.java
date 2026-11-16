package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Jogador;
import com.example.util.Conexao;

public class JogadorDAO {

    private static final String SQL_LISTAR = "SELECT * FROM jogador ORDER BY nome";
    private static final String SQL_LISTAR_POR_TIME = "SELECT * FROM jogador WHERE time_id = ? ORDER BY nome";
    private static final String SQL_BUSCAR = "SELECT * FROM jogador WHERE id = ?";
    private static final String SQL_INSERIR = "INSERT INTO jogador (nome, time_id, gols, assistencias) VALUES (?, ?, ?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE jogador SET nome = ?, time_id = ?, gols = ?, assistencias = ? WHERE id = ?";
    private static final String SQL_DELETAR = "DELETE FROM jogador WHERE id = ?";

    // rankings
    private static final String SQL_RANKING_GOLS =
        "SELECT * FROM jogador ORDER BY gols DESC, assistencias DESC, nome ASC";
    private static final String SQL_RANKING_ASSISTENCIAS =
        "SELECT * FROM jogador ORDER BY assistencias DESC, gols DESC, nome ASC";

    public List<Jogador> listar() throws SQLException {
        return consultar(SQL_LISTAR, null);
    }

    public List<Jogador> listarPorTime(int timeId) throws SQLException {
        return consultar(SQL_LISTAR_POR_TIME, timeId);
    }

    public List<Jogador> rankingGols() throws SQLException {
        return consultar(SQL_RANKING_GOLS, null);
    }

    public List<Jogador> rankingAssistencias() throws SQLException {
        return consultar(SQL_RANKING_ASSISTENCIAS, null);
    }

    public Jogador buscarPorId(int id) throws SQLException {
        Connection con = Conexao.getConexao();
        try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Jogador inserir(Jogador j) throws SQLException {
        Connection con = Conexao.getConexao();
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, j.getNome());
            ps.setInt(2, j.getTimeId());
            ps.setInt(3, j.getGols());
            ps.setInt(4, j.getAssistencias());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    j.setId(keys.getInt(1));
                }
            }
        }
        return j;
    }

    public boolean atualizar(Jogador j) throws SQLException {
        Connection con = Conexao.getConexao();
        try (PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, j.getNome());
            ps.setInt(2, j.getTimeId());
            ps.setInt(3, j.getGols());
            ps.setInt(4, j.getAssistencias());
            ps.setInt(5, j.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        Connection con = Conexao.getConexao();
        try (PreparedStatement ps = con.prepareStatement(SQL_DELETAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Jogador> consultar(String sql, Integer parametro) throws SQLException {
        List<Jogador> lista = new ArrayList<>();
        Connection con = Conexao.getConexao();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Jogador mapear(ResultSet rs) throws SQLException {
        Jogador j = new Jogador();
        j.setId(rs.getInt("id"));
        j.setNome(rs.getString("nome"));
        j.setTimeId(rs.getInt("time_id"));
        j.setGols(rs.getInt("gols"));
        j.setAssistencias(rs.getInt("assistencias"));
        return j;
    }
}