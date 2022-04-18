package br.com.gwsistemas.conexao;

import br.com.gwsistemas.ambiente.Ambiente;
import br.com.gwsistemas.apoio.Apoio;
import br.com.gwsistemas.configuracao.ConfiguracaoSAAS;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class Conexao {
    @SneakyThrows
    @NonNull
    public static Connection criarConexaoSAAS(@NonNull final String estagio) {
        final Instant tempoInicial = Instant.now();

        final ConfiguracaoSAAS configuracaoSAAS = Apoio.carregarConfiguracaoSAAS(estagio);

        if (log.isDebugEnabled()) {
            log.debug(configuracaoSAAS.toString());
        }

        final Connection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%s/%s?user=%s&password=%s&ssl=false",
                        configuracaoSAAS.getBancoHost(),
                        configuracaoSAAS.getBancoNome(),
                        configuracaoSAAS.getBancoUsuario(),
                        configuracaoSAAS.getBancoSenha()
                )
        );

        final Instant tempoFinal = Instant.now();

        if (log.isDebugEnabled()) {
            log.debug("Tempo para a conexão ao RDS SAAS: " + Duration.between(tempoInicial, tempoFinal).toMillis() + " ms");
        }

        return connection;
    }

    @SneakyThrows
    @NonNull
    public static Connection criarConexaoAmbiente(@NonNull final Ambiente ambiente) {
        final Instant tempoInicial = Instant.now();

        if (log.isDebugEnabled()) {
            log.debug("Ambiente: " + ambiente);
        }

        Connection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%s/%s?user=%s&password=%s&ssl=false",
                        ambiente.getLocalBanco(),
                        ambiente.getNomeBanco(),
                        ambiente.getUsuarioBanco(),
                        ambiente.getSenhaBanco()
                )
        );

        final Instant tempoFinal = Instant.now();

        if (log.isDebugEnabled()) {
            log.debug("Tempo para a conexão ao RDS Ambiente: " + Duration.between(tempoInicial, tempoFinal).toMillis() + " ms");
        }

        return connection;
    }
}
