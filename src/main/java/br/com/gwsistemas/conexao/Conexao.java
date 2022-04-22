package br.com.gwsistemas.conexao;

import br.com.gwsistemas.ambiente.Ambiente;
import br.com.gwsistemas.apoio.Apoio;
import br.com.gwsistemas.configuracao.ConfiguracaoSAAS;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Conexao {
    
    private static Logger log = LoggerFactory.getLogger(Conexao.class);
    
    @NonNull
    public static Connection criarConexaoSAAS(@NonNull final String estagio) throws SQLException {
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

    @NonNull
    public static Connection criarConexaoAmbiente(@NonNull final Ambiente ambiente) throws SQLException {
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
