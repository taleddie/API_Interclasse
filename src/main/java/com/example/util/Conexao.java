package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexão com o banco SQLite (arquivo único, sem servidor de banco).
 *
 * O banco e as tabelas são criados automaticamente na primeira conexão.
 */
public class Conexao {

    /** Caminho do arquivo do banco. Pode ser alterado pela variável de ambiente INTERCLASSE_DB. */
    private static final String URL = "jdbc:sqlite:" + caminhoBanco();

    private static Connection conexao = null;

    private static String caminhoBanco() {
        String env = System.getenv("INTERCLASSE_DB");
        if (env == null || env.isBlank()) {
            env = "interclasse.db";
        }
        return env;
    }

    public static Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver do SQLite nao encontrado", e);
            }
            conexao = DriverManager.getConnection(URL);
            criarTabelas(conexao);
        }
        return conexao;
    }

    /**
     * Script das tabelas usadas pela base. Alunos podem ampliar este script
     * conforme implementam as atividades do README.
     */
   private static void criarTabelas(Connection conn) throws SQLException {
        String[] scripts = {
            """
            CREATE TABLE IF NOT EXISTS usuario (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                nome    TEXT    NOT NULL,
                email   TEXT    NOT NULL UNIQUE,
                senha   TEXT    NOT NULL,
                tipo    TEXT    NOT NULL DEFAULT 'PARTICIPANTE'
                        CHECK (tipo IN ('PARTICIPANTE', 'ORGANIZADOR')),
                criado_em TEXT    NOT NULL DEFAULT (datetime('now'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS chave_mestra (
                id    INTEGER PRIMARY KEY AUTOINCREMENT,
                chave TEXT NOT NULL UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS time (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                nome        TEXT    NOT NULL,
                cor         TEXT,
                responsavel TEXT,
                contato     TEXT,
                criado_em   TEXT    NOT NULL DEFAULT (datetime('now'))
            )
            """,
            "INSERT OR IGNORE INTO chave_mestra (chave) VALUES ('ETEC-INTERCLASSE-2026')",
            
            /*** tabelas novas ***/
            """
            CREATE TABLE IF NOT EXISTS turma (
                turmaid INTEGER PRIMARY KEY AUTOINCREMENT,
                nome    TEXT    NOT NULL,
                curso   TEXT    NOT NULL,
                ano     INTEGER NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS modalidade (
                modalidadeid INTEGER PRIMARY KEY AUTOINCREMENT,
                nome         TEXT    NOT NULL,
                ativo        INTEGER NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS inscricao (
                inscricaoid   INTEGER PRIMARY KEY AUTOINCREMENT,
                time_id       INTEGER NOT NULL,
                modalidade_id INTEGER NOT NULL,
                data_inscricao TEXT   NOT NULL DEFAULT (datetime('now')),
                status        TEXT    NOT NULL DEFAULT 'PENDENTE'
                              CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'NEGADA')),
                FOREIGN KEY (time_id) REFERENCES time(id),
                FOREIGN KEY (modalidade_id) REFERENCES modalidade(modalidadeid)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS partida (
                partidaid     INTEGER PRIMARY KEY AUTOINCREMENT,
                modalidade_id INTEGER NOT NULL,
                time_a_id     INTEGER NOT NULL,
                time_b_id     INTEGER NOT NULL,
                placar_a      INTEGER NOT NULL DEFAULT 0,
                placar_b      INTEGER NOT NULL DEFAULT 0,
                data          TEXT,
                hora          TEXT,
                local         TEXT,
                fase          TEXT    NOT NULL DEFAULT 'AVULSO',
                status        TEXT    NOT NULL DEFAULT 'AGENDADA'
                              CHECK (status IN ('AGENDADA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA')),
                FOREIGN KEY (modalidade_id) REFERENCES modalidade(modalidadeid),
                FOREIGN KEY (time_a_id) REFERENCES time(id),
                FOREIGN KEY (time_b_id) REFERENCES time(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS configuracao (
                chave TEXT PRIMARY KEY,
                valor TEXT NOT NULL
            )
            """,

            "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_VITORIA', '3')",
            "INSERT OR IGNORE INTO configuracao (chave, valor) PRIVILEGES INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_EMPATE', '1')",
            "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_DERROTA', '0')",
            "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PERMITE_EMPATE', '1')"
        };

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            for (String script : scripts) {
                stmt.execute(script);
            }
        }
    }
}
