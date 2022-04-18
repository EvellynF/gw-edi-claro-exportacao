package br.com.gwsistemas.ambiente;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
@RequiredArgsConstructor
public class AmbienteDAO {
    private final Connection con;

    @SneakyThrows
    protected Ambiente carregarAmbientePorChaveOrganizacao(@NonNull final String chaveOrganizacao) {
        int index = 0;

        @Cleanup
        final PreparedStatement prepSt = this.con.prepareStatement("SELECT tba.id_ambiente, tbo.id_organizacao, tba.versao, tba.local_db, tba.nm_db, tba.usuario_db, tba.senha_db, tba.url_aplicacao " +
                "FROM tb_organizacao tbo " +
                "LEFT JOIN tb_ambiente tba ON (tbo.ambiente_id = tba.id_ambiente) " +
                "WHERE tbo.chave = ? AND tbo.is_ativo AND tba.is_ativo LIMIT 1;");

        prepSt.setString(++index, chaveOrganizacao);

        @Cleanup
        final ResultSet rs = prepSt.executeQuery();
        
        if (rs.next()) {

            Ambiente ambiente = new Ambiente();

            ambiente.setId(rs.getInt("id_ambiente"));
            ambiente.setNome(rs.getString("id_organizacao"));
            ambiente.setVersao(rs.getString("versao"));
            ambiente.setLocalBanco(rs.getString("local_db"));
            ambiente.setNomeBanco(rs.getString("nm_db"));
            ambiente.setUsuarioBanco(rs.getString("usuario_db"));
            ambiente.setSenhaBanco(rs.getString("senha_db"));
            ambiente.setUrlAplicacao(rs.getString("url_aplicacao"));
            ambiente.setOrganizacaoId(rs.getInt("id_organizacao"));

            return ambiente;
        }

        return null;
    }
}
