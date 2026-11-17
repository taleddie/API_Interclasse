package com.example.model;

/**
 * Entidade Modalidade.
 * ativo = 1 (disponivel) ou 0 (inativa).
 */
public class Modalidade {

    private Integer id;
    private String nome;
    private Integer ativo;

    public Modalidade() {
    }

    public Modalidade(String nome, Integer ativo) {
        this.nome = nome;
        this.ativo = ativo;
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

    public Integer getAtivo() {
        return ativo;
    }

    public void setAtivo(Integer ativo) {
        this.ativo = ativo;
    }
}
