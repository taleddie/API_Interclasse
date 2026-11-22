package com.example.dao;

import com.example.model.Configuracao;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracaoDAO {

    private static final String SQL_LISTAR = "SELECT * FROM configuracao ORDER BY chave";
    private static final String SQL_BUSCAR = "SELECT * FROM configuracao WHERE chave = ?";
    private static final String SQL_ATUALIZAR = "UPDATE configuracao SET valor = ? WHERE chave = ?";

    public List<Configuracao> listar() throws SQLException {
        List<Configuracao> lista = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
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

    public boolean atualizar(Configuracao config) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, config.getValor());
            ps.setString(2, config.getChave());
            return ps.executeUpdate() > 0;
        }
    }

    public int buscarInt(String chave, int padrao) throws SQLException {
        Configuracao config = buscarPorChave(chave);
        if (config == null || config.getValor() == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(config.getValor().trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private Configuracao mapear(ResultSet rs) throws SQLException {
        return new Configuracao(rs.getString("chave"), rs.getString("valor"));
    }
}