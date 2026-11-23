package com.example.dao;

import com.example.model.Usuario;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO de Usuario (base do sistema de login).
 */
public class UsuarioDAO {

    private static final String SQL_BUSCAR_EMAIL = "SELECT * FROM usuario WHERE email = ?";
    private static final String SQL_BUSCAR_ID = "SELECT * FROM usuario WHERE id = ?";
    private static final String SQL_INSERIR = "INSERT INTO usuario (nome, email, senha, tipo) VALUES (?, ?, ?, ?)";
    private static final String SQL_CHAVE_EXISTE = "SELECT COUNT(*) FROM chave_mestra WHERE chave = ?";
    private static final String SQL_CHAVE_CADASTRO = "INSERT INTO chave_mestra (chave) VALUES (?)";

    public Usuario buscarPorEmail(String email) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario inserir(Usuario usuario) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.setString(4, usuario.getTipo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getInt(1));
                }
            }
        }
        return usuario;
    }

    /** Verifica se a chave informada existe na tabela de chaves mestras. */
    public boolean chaveExiste(String chave) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHAVE_EXISTE)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Cadastra uma nova chave mestra (usado para distribuir acessos aos organizadores). */
    public void cadastrarChave(String chave) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHAVE_CADASTRO)) {
            ps.setString(1, chave);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTipo(rs.getString("tipo"));
        return usuario;
    }
}
