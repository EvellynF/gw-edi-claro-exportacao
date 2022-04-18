package br.com.gwsistemas.configuracao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@Data
public class ConfiguracaoSAAS {
    private int tempoSessao;
    @NonNull
    private String bancoHost;
    private int bancoPorta;
    @NonNull
    private String bancoUsuario;
    @NonNull
    private String bancoSenha;

    @NonNull
    private String bancoNome;
}
