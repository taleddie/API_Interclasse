package com.example.dao;

<<<<<<< HEAD
import com.example.model.Modalidade;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ModalidadeDAO {

    private static final String SQL_LISTAR = "SELECT modalidadeid AS id, nome, ativo FROM modalidade ORDER BY nome";
    private static final String SQL_BUSCAR = "SELECT modalidadeid AS id, nome, ativo FROM modalidade WHERE modalidadeid = ?";
    private static final String SQL_INSERIR = "INSERT INTO modalidade (nome, ativo) VALUES (?, COALESCE(?, 1))";
    private static final String SQL_ATUALIZAR = "UPDATE modalidade SET nome = ?, ativo = COALESCE(?, 1) WHERE modalidadeid = ?";
    private static final String SQL_DELETAR = "DELETE FROM modalidade WHERE modalidadeid = ?";

    public List<Modalidade> listar() throws SQLException {
        List<Modalidade> modalidades = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modalidades.add(mapear(rs));
            }
        }
        return modalidades;
    }

    public Modalidade buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Modalidade inserir(Modalidade modalidade) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, modalidade.getNome());
            ps.setObject(2, modalidade.getAtivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    modalidade.setId(keys.getInt(1));
                }
            }
        }
        return modalidade;
    }

    public boolean atualizar(Modalidade modalidade) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, modalidade.getNome());
            ps.setObject(2, modalidade.getAtivo());
            ps.setInt(3, modalidade.getId());
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

    private Modalidade mapear(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(rs.getInt("id"));
        modalidade.setNome(rs.getString("nome"));
        modalidade.setAtivo(rs.getInt("ativo"));
        return modalidade;
    }
=======
public class ModalidadeDAO {
>>>>>>> 11906ed5b6a565a919bd8e422d62f6577006cc64
}
