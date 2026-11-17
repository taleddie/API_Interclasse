package com.example.model;

public class Inscricao {

    private Integer id;
    private Integer timeId;
    private Integer modalidadeId;
    private String dataInscricao;
    private String status;

    public Inscricao() {
    }

    public Inscricao(Integer timeId, Integer modalidadeId, String dataInscricao, String status) {
        this.timeId = timeId;
        this.modalidadeId = modalidadeId;
        this.dataInscricao = dataInscricao;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTimeId() {
        return timeId;
    }

    public void setTimeId(Integer timeId) {
        this.timeId = timeId;
    }

    public Integer getModalidadeId() {
        return modalidadeId;
    }

    public void setModalidadeId(Integer modalidadeId) {
        this.modalidadeId = modalidadeId;
    }

    public String getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(String dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
