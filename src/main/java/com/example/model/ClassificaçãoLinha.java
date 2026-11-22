package com.example.model;

public class ClassificacaoLinha {

    private int timeId;
    private String time;
    private int jogos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsPro;
    private int golsContra;
    private int saldo;
    private int pontos;

    public ClassificacaoLinha(int timeId, String time) {
        this.timeId = timeId;
        this.time = time;
    }

    public void registrarJogo(int pro, int contra, int pontosVitoria, int pontosEmpate, int pontosDerrota) {
        jogos++;
        golsPro += pro;
        golsContra += contra;
        saldo = golsPro - golsContra;

        if (pro > contra) {
            vitorias++;
            pontos += pontosVitoria;
        } else if (pro == contra) {
            empates++;
            pontos += pontosEmpate;
        } else {
            derrotas++;
            pontos += pontosDerrota;
        }
    }

    public int getTimeId() { return timeId; }
    public String getTime() { return time; }
    public int getJogos() { return jogos; }
    public int getVitorias() { return vitorias; }
    public int getEmpates() { return empates; }
    public int getDerrotas() { return derrotas; }
    public int getGolsPro() { return golsPro; }
    public int getGolsContra() { return golsContra; }
    public int getSaldo() { return saldo; }
    public int getPontos() { return pontos; }
}