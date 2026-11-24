package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Time;
import com.example.util.Conexao;

/**
 * DAO de exemplo com as operacoes basicas de um CRUD.
 * Os alunos podem usar este arquivo como modelo para criar os demais DAOs.
 */
public class TimeDAO {

    private static final String SQL_LISTAR = "SELECT * FROM time ORDER BY nome";
    private static final String SQL_BUSCAR = "SELECT * FROM time WHERE id = ?";
    private static final String SQL_INSERIR = "INSERT INTO time (nome, cor, responsavel, contato) VALUES (?, ?, ?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE time SET nome = ?, cor = ?, responsavel = ?, contato = ? WHERE id = ?";
    private static final String SQL_DELETAR = "DELETE FROM time WHERE id = ?";

    public List<Time> listar() throws SQLException {
        List<Time> times = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                times.add(mapear(rs));
            }
        }
        return times;
    }

    public Time buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Time inserir(Time time) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, time.getNome());
            ps.setString(2, time.getCor());
            ps.setString(3, time.getResponsavel());
            ps.setString(4, time.getContato());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    time.setId(keys.getInt(1));
                }
            }
        }
        return time;
    }

    public boolean atualizar(Time time) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, time.getNome());
            ps.setString(2, time.getCor());
            ps.setString(3, time.getResponsavel());
            ps.setString(4, time.getContato());
            ps.setInt(5, time.getId());
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

    private Time mapear(ResultSet rs) throws SQLException {
        Time time = new Time();
        time.setId(rs.getInt("id"));
        time.setNome(rs.getString("nome"));
        time.setCor(rs.getString("cor"));
        time.setResponsavel(rs.getString("responsavel"));
        time.setContato(rs.getString("contato"));
        return time;
    }
}
