package com.example.model;

/**
 * Entidade Turma.
 */
public class Turma {

    private Integer id;
    private String nome;
    private String curso;
    private Integer ano;

    public Turma() {
    }

    public Turma(String nome, String curso, Integer ano) {
        this.nome = nome;
        this.curso = curso;
        this.ano = ano;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }
}
