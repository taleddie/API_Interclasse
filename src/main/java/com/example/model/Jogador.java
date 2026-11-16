package com.example.model;

public class Jogador {

    private int id;
    private String nome;
    private int timeId;
    private int gols;
    private int assistencias;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getTimeId() { return timeId; }
    public void setTimeId(int timeId) { this.timeId = timeId; }

    public int getGols() { return gols; }
    public void setGols(int gols) { this.gols = gols; }

    public int getAssistencias() { return assistencias; }
    public void setAssistencias(int assistencias) { this.assistencias = assistencias; }
}