package br.com.gwsistemas.ambiente;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
public class AmbienteBO {
    private final Connection connectionAmbiente;

    public Ambiente carregarAmbientePorChaveOrganizacao(final String chaveOrganizacao) {
        return new AmbienteDAO(this.connectionAmbiente).carregarAmbientePorChaveOrganizacao(chaveOrganizacao);
    }
}
