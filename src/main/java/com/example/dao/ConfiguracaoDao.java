package com.example.dao;

import com.example.model.Configuracao;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracaoDao {

    private static final String SQL_LISTAR = "SELECT chave, valor FROM configuracao ORDER BY chave";
    private static final String SQL_BUSCAR = "SELECT chave, valor FROM configuracao WHERE chave = ?";
    private static final String SQL_INSERIR = "INSERT INTO configuracao (chave, valor) VALUES (?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE configuracao SET valor = ? WHERE chave = ?";
    private static final String SQL_DELETAR = "DELETE FROM configuracao WHERE chave = ?";

    public List<Configuracao> listar() throws SQLException {
        List<Configuracao> configuracoes = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                configuracoes.add(mapear(rs));
            }
        }
        return configuracoes;
    }

    public Configuracao buscarPorChave(String chave) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Configuracao inserir(Configuracao configuracao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR)) {
            ps.setString(1, configuracao.getChave());
            ps.setString(2, configuracao.getValor());
            ps.executeUpdate();
        }
        return configuracao;
    }

    public boolean atualizar(Configuracao configuracao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, configuracao.getValor());
            ps.setString(2, configuracao.getChave());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(String chave) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETAR)) {
            ps.setString(1, chave);
            return ps.executeUpdate() > 0;
        }
    }

    private Configuracao mapear(ResultSet rs) throws SQLException {
        return new Configuracao(rs.getString("chave"), rs.getString("valor"));
    }
}
