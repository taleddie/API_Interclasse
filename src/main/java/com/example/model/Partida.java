package com.example.model;

/**
 * Entidade Partida.
 * status = AGENDADA, EM_ANDAMENTO, FINALIZADA ou CANCELADA.
 * fase = AVULSO, GRUPOS, QUARTAS, SEMI ou FINAL.
 */
public class Partida {

    private Integer id;
    private Integer modalidadeId;
    private Integer timeAId;
    private Integer timeBId;
    private Integer placarA;
    private Integer placarB;
    private String data;
    private String hora;
    private String local;
    private String fase;
    private String status;

    public Partida() {
    }

    public Partida(Integer modalidadeId, Integer timeAId, Integer timeBId, Integer placarA, Integer placarB,
                   String data, String hora, String local, String fase, String status) {
        this.modalidadeId = modalidadeId;
        this.timeAId = timeAId;
        this.timeBId = timeBId;
        this.placarA = placarA;
        this.placarB = placarB;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.fase = fase;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModalidadeId() {
        return modalidadeId;
    }

    public void setModalidadeId(Integer modalidadeId) {
        this.modalidadeId = modalidadeId;
    }

    public Integer getTimeAId() {
        return timeAId;
    }

    public void setTimeAId(Integer timeAId) {
        this.timeAId = timeAId;
    }

    public Integer getTimeBId() {
        return timeBId;
    }

    public void setTimeBId(Integer timeBId) {
        this.timeBId = timeBId;
    }

    public Integer getPlacarA() {
        return placarA;
    }

    public void setPlacarA(Integer placarA) {
        this.placarA = placarA;
    }

    public Integer getPlacarB() {
        return placarB;
    }

    public void setPlacarB(Integer placarB) {
        this.placarB = placarB;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
