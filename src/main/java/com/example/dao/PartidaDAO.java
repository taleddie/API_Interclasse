package com.example.dao;

<<<<<<< HEAD
import com.example.model.Partida;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAO {

    private static final String SQL_LISTAR =
        "SELECT partidaid AS id, modalidade_id, time_a_id, time_b_id, placar_a, placar_b, " +
        "data, hora, local, fase, status FROM partida ORDER BY data, hora, partidaid";
    private static final String SQL_LISTAR_POR_MODALIDADE =
        "SELECT partidaid AS id, modalidade_id, time_a_id, time_b_id, placar_a, placar_b, " +
        "data, hora, local, fase, status FROM partida WHERE modalidade_id = ? ORDER BY data, hora, partidaid";
    private static final String SQL_BUSCAR =
        "SELECT partidaid AS id, modalidade_id, time_a_id, time_b_id, placar_a, placar_b, " +
        "data, hora, local, fase, status FROM partida WHERE partidaid = ?";
    private static final String SQL_INSERIR =
        "INSERT INTO partida (modalidade_id, time_a_id, time_b_id, placar_a, placar_b, data, hora, local, fase, status) " +
        "VALUES (?, ?, ?, COALESCE(?, 0), COALESCE(?, 0), ?, ?, ?, COALESCE(?, 'AVULSO'), COALESCE(?, 'AGENDADA'))";
    private static final String SQL_ATUALIZAR =
        "UPDATE partida SET modalidade_id = ?, time_a_id = ?, time_b_id = ?, " +
        "placar_a = COALESCE(?, 0), placar_b = COALESCE(?, 0), data = ?, hora = ?, local = ?, " +
        "fase = COALESCE(?, 'AVULSO'), status = COALESCE(?, 'AGENDADA') WHERE partidaid = ?";
    private static final String SQL_DELETAR = "DELETE FROM partida WHERE partidaid = ?";

    public List<Partida> listar() throws SQLException {
        return consultar(SQL_LISTAR, null);
    }

    public List<Partida> listarPorModalidade(int modalidadeId) throws SQLException {
        return consultar(SQL_LISTAR_POR_MODALIDADE, modalidadeId);
    }

    public Partida buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Partida inserir(Partida partida) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, partida.getModalidadeId());
            ps.setInt(2, partida.getTimeAId());
            ps.setInt(3, partida.getTimeBId());
            ps.setObject(4, partida.getPlacarA());
            ps.setObject(5, partida.getPlacarB());
            ps.setString(6, partida.getData());
            ps.setString(7, partida.getHora());
            ps.setString(8, partida.getLocal());
            ps.setString(9, partida.getFase());
            ps.setString(10, partida.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    partida.setId(keys.getInt(1));
                }
            }
        }
        return partida;
    }

    public boolean atualizar(Partida partida) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setInt(1, partida.getModalidadeId());
            ps.setInt(2, partida.getTimeAId());
            ps.setInt(3, partida.getTimeBId());
            ps.setObject(4, partida.getPlacarA());
            ps.setObject(5, partida.getPlacarB());
            ps.setString(6, partida.getData());
            ps.setString(7, partida.getHora());
            ps.setString(8, partida.getLocal());
            ps.setString(9, partida.getFase());
            ps.setString(10, partida.getStatus());
            ps.setInt(11, partida.getId());
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

    private List<Partida> consultar(String sql, Integer modalidadeId) throws SQLException {
        List<Partida> partidas = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (modalidadeId != null) {
                ps.setInt(1, modalidadeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    partidas.add(mapear(rs));
                }
            }
        }
        return partidas;
    }

    private Partida mapear(ResultSet rs) throws SQLException {
        Partida partida = new Partida();
        partida.setId(rs.getInt("id"));
        partida.setModalidadeId(rs.getInt("modalidade_id"));
        partida.setTimeAId(rs.getInt("time_a_id"));
        partida.setTimeBId(rs.getInt("time_b_id"));
        partida.setPlacarA(rs.getInt("placar_a"));
        partida.setPlacarB(rs.getInt("placar_b"));
        partida.setData(rs.getString("data"));
        partida.setHora(rs.getString("hora"));
        partida.setLocal(rs.getString("local"));
        partida.setFase(rs.getString("fase"));
        partida.setStatus(rs.getString("status"));
        return partida;
    }
=======
public class PartidaDAO {
>>>>>>> 11906ed5b6a565a919bd8e422d62f6577006cc64
}
