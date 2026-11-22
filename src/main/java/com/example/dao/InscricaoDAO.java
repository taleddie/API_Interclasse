package com.example.dao;

import com.example.model.Inscricao;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InscricaoDAO {

    private static final String SQL_LISTAR =
        "SELECT inscricaoid AS id, time_id, modalidade_id, data_inscricao, status " +
        "FROM inscricao ORDER BY data_inscricao DESC, inscricaoid DESC";
    private static final String SQL_LISTAR_POR_MODALIDADE =
        "SELECT inscricaoid AS id, time_id, modalidade_id, data_inscricao, status " +
        "FROM inscricao WHERE modalidade_id = ? ORDER BY data_inscricao DESC, inscricaoid DESC";
    private static final String SQL_BUSCAR =
        "SELECT inscricaoid AS id, time_id, modalidade_id, data_inscricao, status " +
        "FROM inscricao WHERE inscricaoid = ?";
    private static final String SQL_INSERIR =
        "INSERT INTO inscricao (time_id, modalidade_id, data_inscricao, status) " +
        "VALUES (?, ?, COALESCE(?, datetime('now')), COALESCE(?, 'PENDENTE'))";
    private static final String SQL_ATUALIZAR =
        "UPDATE inscricao SET time_id = ?, modalidade_id = ?, " +
        "data_inscricao = COALESCE(?, datetime('now')), status = COALESCE(?, 'PENDENTE') " +
        "WHERE inscricaoid = ?";
    private static final String SQL_DELETAR = "DELETE FROM inscricao WHERE inscricaoid = ?";
    private static final String SQL_EXISTE = "SELECT 1 FROM inscricao WHERE time_id = ? LIMIT 1";
    private static final String SQL_ESTA_CONFIRMADO =
        "SELECT 1 FROM inscricao WHERE time_id = ? AND modalidade_id = ? AND status = 'CONFIRMADA' LIMIT 1";

    public List<Inscricao> listar() throws SQLException {
        return consultar(SQL_LISTAR, null);
    }

    public List<Inscricao> listarPorModalidade(int modalidadeId) throws SQLException {
        return consultar(SQL_LISTAR_POR_MODALIDADE, modalidadeId);
    }

    public Inscricao buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Inscricao inserir(Inscricao inscricao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inscricao.getTimeId());
            ps.setInt(2, inscricao.getModalidadeId());
            ps.setString(3, inscricao.getDataInscricao());
            ps.setString(4, inscricao.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    inscricao.setId(keys.getInt(1));
                }
            }
        }
        return inscricao;
    }

    public boolean atualizar(Inscricao inscricao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setInt(1, inscricao.getTimeId());
            ps.setInt(2, inscricao.getModalidadeId());
            ps.setString(3, inscricao.getDataInscricao());
            ps.setString(4, inscricao.getStatus());
            ps.setInt(5, inscricao.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeInscricao(int timeId) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTE)) {
            ps.setInt(1, timeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean estaConfirmado(int timeId, int modalidadeId) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ESTA_CONFIRMADO)) {
            ps.setInt(1, timeId);
            ps.setInt(2, modalidadeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Inscricao> consultar(String sql, Integer modalidadeId) throws SQLException {
        List<Inscricao> inscricoes = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (modalidadeId != null) {
                ps.setInt(1, modalidadeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    inscricoes.add(mapear(rs));
                }
            }
        }
        return inscricoes;
    }

    private Inscricao mapear(ResultSet rs) throws SQLException {
        Inscricao inscricao = new Inscricao();
        inscricao.setId(rs.getInt("id"));
        inscricao.setTimeId(rs.getInt("time_id"));
        inscricao.setModalidadeId(rs.getInt("modalidade_id"));
        inscricao.setDataInscricao(rs.getString("data_inscricao"));
        inscricao.setStatus(rs.getString("status"));
        return inscricao;
    }
}
