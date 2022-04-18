package br.com.gwsistemas.ambiente;

import lombok.Data;

@Data
public class Ambiente {
    private int id;
    private String nome;
    private String versao;
    private String localBanco;
    private String nomeBanco;
    private String usuarioBanco;
    private String senhaBanco;
    private String urlAplicacao;
    private int organizacaoId;
}
