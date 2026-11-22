package com.example.dao;

<<<<<<< HEAD
import com.example.model.Turma;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    private static final String SQL_LISTAR = "SELECT turmaid AS id, nome, curso, ano FROM turma ORDER BY nome";
    private static final String SQL_BUSCAR = "SELECT turmaid AS id, nome, curso, ano FROM turma WHERE turmaid = ?";
    private static final String SQL_INSERIR = "INSERT INTO turma (nome, curso, ano) VALUES (?, ?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE turma SET nome = ?, curso = ?, ano = ? WHERE turmaid = ?";
    private static final String SQL_DELETAR = "DELETE FROM turma WHERE turmaid = ?";

    public List<Turma> listar() throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                turmas.add(mapear(rs));
            }
        }
        return turmas;
    }

    public Turma buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Turma inserir(Turma turma) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, turma.getNome());
            ps.setString(2, turma.getCurso());
            ps.setInt(3, turma.getAno());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    turma.setId(keys.getInt(1));
                }
            }
        }
        return turma;
    }

    public boolean atualizar(Turma turma) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, turma.getNome());
            ps.setString(2, turma.getCurso());
            ps.setInt(3, turma.getAno());
            ps.setInt(4, turma.getId());
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

    private Turma mapear(ResultSet rs) throws SQLException {
        Turma turma = new Turma();
        turma.setId(rs.getInt("id"));
        turma.setNome(rs.getString("nome"));
        turma.setCurso(rs.getString("curso"));
        turma.setAno(rs.getInt("ano"));
        return turma;
    }
=======
public class TurmaDAO {
>>>>>>> 11906ed5b6a565a919bd8e422d62f6577006cc64
}
