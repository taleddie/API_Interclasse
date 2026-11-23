package com.example.controller;

import com.example.dao.UsuarioDAO;
import com.example.model.Usuario;
import com.example.util.JsonUtil;
import com.example.util.SessaoUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller de autenticacao.
 *
 * Rotas:
 *   POST /api/auth/registrar           -> cadastro do participante (livre)
 *   POST /api/auth/registrar-organizador-> cadastro do organizador (exige chave mestra)
 *   POST /api/auth/login                -> faz login
 *   POST /api/auth/logout               -> faz logout
 *   GET  /api/auth/me                   -> dados do usuario logado
 */
@WebServlet("/auth/*")
public class AuthController extends HttpServlet {

    private UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = acao(request);

        if ("me".equals(acao)) {
            Integer id = SessaoUtil.usuarioLogado(request);
            if (id == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, "nao ha usuario logado");
                return;
            }
            try {
                Usuario usuario = dao.buscarPorId(id);
                if (usuario == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "usuario nao encontrado");
                    return;
                }
                usuario.setSenha(null);
                JsonUtil.enviar(response, usuario);
            } catch (SQLException e) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return;
        }

        JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "rota nao encontrada");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acao = acao(request);

        try {
            switch (acao) {
                case "registrar":
                    registrar(request, response, false);
                    break;
                case "registrar-organizador":
                    registrar(request, response, true);
                    break;
                case "login":
                    login(request, response);
                    break;
                case "logout":
                    logout(request, response);
                    break;
                default:
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "rota nao encontrada");
            }
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /** Cadastro de participante (livre) ou de organizador (com chave mestra). */
    private void registrar(HttpServletRequest request, HttpServletResponse response, boolean organizador) throws IOException, SQLException {
        Credencial credencial = JsonUtil.ler(request, Credencial.class);

        if (credencial == null
                || credencial.nome == null || credencial.nome.isBlank()
                || credencial.email == null || credencial.email.isBlank()
                || credencial.senha == null || credencial.senha.isBlank()) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "nome, email e senha sao obrigatorios");
            return;
        }

        if (organizador) {
            if (credencial.chaveMestra == null || !dao.chaveExiste(credencial.chaveMestra)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_FORBIDDEN, "chave mestra invalida");
                return;
            }
        }

        if (dao.buscarPorEmail(credencial.email) != null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "email ja cadastrado");
            return;
        }

        Usuario usuario = new Usuario(
                credencial.nome,
                credencial.email,
                SessaoUtil.hashSenha(credencial.senha),
                organizador ? "ORGANIZADOR" : "PARTICIPANTE"
        );
        dao.inserir(usuario);
        usuario.setSenha(null);
        JsonUtil.enviar(response, usuario);
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        Credencial credencial = JsonUtil.ler(request, Credencial.class);

        if (credencial == null || credencial.email == null || credencial.senha == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "email e senha sao obrigatorios");
            return;
        }

        Usuario usuario = dao.buscarPorEmail(credencial.email);
        if (usuario == null || !usuario.getSenha().equals(SessaoUtil.hashSenha(credencial.senha))) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, "email ou senha invalidos");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SessaoUtil.ATRIBUTO_USUARIO, usuario.getId());
        session.setAttribute(SessaoUtil.ATRIBUTO_TIPO, usuario.getTipo());

        usuario.setSenha(null);
        JsonUtil.enviar(response, usuario);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "logout realizado");
    }

    /** Le a ultima parte da URL (ex: /api/auth/login -> login). */
    private String acao(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.substring(1);
    }

    /** Objeto de entrada do login/cadastro. */
    public static class Credencial {
        public String nome;
        public String email;
        public String senha;
        public String chaveMestra;
    }
}
