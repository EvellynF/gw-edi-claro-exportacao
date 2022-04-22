package br.com.gwsistemas.exportacaoEdiClaro;

import br.com.gwsistemas.ambiente.Ambiente;
import br.com.gwsistemas.ambiente.AmbienteBO;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import br.com.gwsistemas.cliente.Cliente;

import br.com.gwsistemas.claro.bean.ArrayOfCTEV2;
import br.com.gwsistemas.claro.bean.ArrayOfClassRetornoCTEV2;
import br.com.gwsistemas.claro.bean.ArrayOfDocumentoFiscalV2;
import br.com.gwsistemas.claro.bean.ArrayOfOcorrenciaTransporteV2;
import br.com.gwsistemas.claro.bean.ClassAuthenticationV2;
import br.com.gwsistemas.claro.bean.ReceberConhecimentosResult;
import br.com.gwsistemas.claro.bean.ReceberRastreamentoResult;
import br.com.gwsistemas.claro.bean.RetornoDocumentoFiscalV2;
import br.com.gwsistemas.claro.bean.RetornoOcorrenciaTransporteV2;
import br.com.gwsistemas.claro.servicos.ChamadaServicos;
import br.com.gwsistemas.conexao.Conexao;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportacaoEdiClaroBO {
    
    Logger log = LoggerFactory.getLogger(ExportacaoEdiClaroBO.class);    
    
    private String chaveOrganizacao;
    private String estagio;
    private Connection con;
    
    public ExportacaoEdiClaroBO(String chaveOrganizacao, String estagio){
        this.chaveOrganizacao = chaveOrganizacao;
        this.estagio = estagio;
    }
    
    public ClassAuthenticationV2 doAutenticacao(Cliente cliente) {
            ClassAuthenticationV2 autenticacao = new ClassAuthenticationV2();
            autenticacao.setCNPJTransportadora(
                            cliente.getLayoutsCONEMB().stream().filter(Objects::nonNull).findFirst().get().getLogin());
            autenticacao.setChaveAcesso(
                            cliente.getLayoutsCONEMB().stream().filter(Objects::nonNull).findFirst().get().getSenha());
            autenticacao.setRazaoSocial(cliente.getRazaosocial());
            return autenticacao;
    }
    
    public StringBuilder enviarConembClaro(int idConsignatario, String dataInicio, String dataFim, String ids) throws SQLException{
        ArrayOfDocumentoFiscalV2 arrayDOC;
        ClassAuthenticationV2 autenticacao;
        ChamadaServicos server;
        ReceberConhecimentosResult arrayRetorno = null;
        StringBuilder resposta = null;
        ExportacaoEdiClaroDAO dao = null;
        ArrayList<Cliente> clientes = null;
        try {
            resposta = new StringBuilder();
            dao = new ExportacaoEdiClaroDAO(this.getConexao());
            server = new ChamadaServicos();
            clientes = dao.obterDadosAutorizacao(idConsignatario, dataInicio, dataFim, ids, "webserviceClaroConemb");
            for (Cliente cliente : clientes) {
                String senha = cliente.getLayoutsCONEMB().stream().filter(Objects::nonNull).findFirst().get().getSenha();
                if ("".equals(senha)) {
                    resposta.append(cliente.getRazaosocial()).append(", Não possui acesso ao WS Claro S/A, CONEMB dele não foram enviados! ");
                    continue;
                } else {
                    autenticacao = doAutenticacao(cliente);
                }

                arrayDOC = dao.montarConembCte(idConsignatario, dataInicio, dataFim, ids);

                arrayRetorno = server.enviarConembClaro(arrayDOC, autenticacao);

                if (arrayRetorno != null && arrayRetorno.getDocumentosFiscais() != null && !arrayRetorno.getDocumentosFiscais().isEmpty()) {
                    for (RetornoDocumentoFiscalV2 ret : arrayRetorno.getDocumentosFiscais().stream().filter(ct -> !ct.isDocumentoFiscalImportado()).collect(Collectors.toList())) {
                        resposta.append(" CTE :").append(ret.getNumero()).append("-").append(ret.getSerie()).append(" ").append(ret.getMensagem()).append(";  ");
                    }
                }
            }
            if (clientes.isEmpty()) {
                resposta.append(" Nenhuma informação exportável encontrada. ");
            } else if (resposta.length() == 0) {
                resposta.append(" Sincronização realizada com sucesso. ");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            arrayDOC = null;
            autenticacao = null;
            server = null;
            arrayRetorno = null;
            dao = null;
            if (clientes != null) {
                clientes.clear();
                clientes = null;
            }
            if (this.con != null && !this.con.isClosed()) {
                this.con.close();
            }
        }
        return resposta;
    }

    public StringBuilder enviarOcorenClaro(int idConsignatario, String dataInicio, String dataFim, String ids) throws SQLException{
        ArrayOfOcorrenciaTransporteV2 arrayDOC;
        ClassAuthenticationV2 autenticacao;
        ChamadaServicos server;
        ReceberRastreamentoResult arrayRetorno = null;
        StringBuilder resposta = null;
        ExportacaoEdiClaroDAO dao = null;
        ArrayList<Cliente> clientes = null;
        try {
            resposta = new StringBuilder();
            dao = new ExportacaoEdiClaroDAO(this.getConexao());
            server = new ChamadaServicos();
            clientes = dao.obterDadosAutorizacao(idConsignatario, dataInicio, dataFim, ids , "webserviceClaroOcoren");
            for (Cliente cliente : clientes) {
                String senha = cliente.getLayoutsCONEMB().stream().filter(Objects::nonNull).findFirst().get().getSenha();
                if ("".equals(senha)) {
                    resposta.append(cliente.getRazaosocial()).append(", Não possui acesso ao WS Claro S/A, CONEMB dele não foram enviados! ");
                    continue;
                } else {
                    autenticacao = doAutenticacao(cliente);
                }

                arrayDOC = dao.montarOcorenCte(idConsignatario, dataInicio, dataFim, ids);

                arrayRetorno = server.enviarOcorrenClaro(arrayDOC, autenticacao);

                if (arrayRetorno != null && arrayRetorno.getOcorrenciaTransporte()!= null && !arrayRetorno.getOcorrenciaTransporte().isEmpty()) {
                    for (RetornoOcorrenciaTransporteV2 ret : arrayRetorno.getOcorrenciaTransporte().stream().filter(ct -> !ct.isOcorrenciaImportada()).collect(Collectors.toList())) {
                        resposta.append(" NOTA :").append(ret.getNotasFiscais().get(0).getNumeroNotaFiscal()).append("-")
                                .append(ret.getNotasFiscais().get(0).getSerieNotaFiscal()).append(" ").append(ret.getMensagem()).append(";  ");
                    }
                }
            }
            if (clientes.isEmpty()) {
                resposta.append(" Nenhuma informação exportável encontrada. ");
            } else if (resposta.length() == 0) {
                resposta.append(" Sincronização realizada com sucesso. ");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            arrayDOC = null;
            autenticacao = null;
            server = null;
            arrayRetorno = null;
            dao = null;
            if (clientes != null) {
                clientes.clear();
                clientes = null;
            }
            if (this.con != null && !this.con.isClosed()) {
                this.con.close();
            }
        }
        return resposta;
    }
    
    public StringBuilder enviarXmlClaro(int idConsignatario, String dataInicio, String dataFim, String ids, String chaveOrganizacao) throws SQLException{
        ArrayOfCTEV2 arrayCte;
        ClassAuthenticationV2 autenticacao;
        ChamadaServicos server;
        ArrayOfClassRetornoCTEV2 arrayRetorno = null;
        StringBuilder resposta = null;
        ExportacaoEdiClaroDAO dao = null;
        ArrayList<Cliente> clientes = null;
        try {
            resposta = new StringBuilder();
            dao = new ExportacaoEdiClaroDAO(this.getConexao());
            server = new ChamadaServicos();
            clientes = dao.obterDadosAutorizacao(idConsignatario, dataInicio, dataFim, ids, "webserviceClaro");
            for (Cliente cliente : clientes) {
                String senha = cliente.getLayoutsCONEMB().stream().filter(Objects::nonNull).findFirst().get().getSenha();
                if ("".equals(senha)) {
                    resposta.append(cliente.getRazaosocial()).append(", Não possui acesso ao WS Claro S/A, XMLs dele não foram enviados! ");
                    continue;
                } else {
                    autenticacao = doAutenticacao(cliente);
                }

                arrayCte = dao.montarArrayCte(idConsignatario, dataInicio, dataFim, ids);

                arrayRetorno = server.enviarXmlClaro(arrayCte, autenticacao);

                if (arrayRetorno != null && arrayRetorno.getClassRetornoCTEV2() != null && !arrayRetorno.getClassRetornoCTEV2().isEmpty()) {
                    for (ArrayOfClassRetornoCTEV2.ClassRetornoCTEV2 ret
                            : arrayRetorno.getClassRetornoCTEV2().stream().filter(cte -> cte.isErroNaRequisicao()).collect(Collectors.toList())) {
                        resposta.append(" CTE :").append(ret.getNumeroChaveConhecimento()).append(" ").append(ret.getMensagem()).append(";  ");
                    }
                }
            }
            if (clientes.isEmpty()) {
                resposta.append(" Nenhuma informaçãoo exportável encontrada. ");
            } else if (resposta.length() == 0) {
                resposta.append(" Sincronização realizada com sucesso. ");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            arrayCte = null;
            autenticacao = null;
            server = null;
            arrayRetorno = null;
            dao = null;
            if (clientes != null) {
                clientes.clear();
                clientes = null;
            }
            if (this.con != null && !this.con.isClosed()) {
                this.con.close();
            }
        }
        return resposta;
    }
    
    private Connection getConexao() throws SQLException{
        Conexao conexao = new Conexao();
        AmbienteBO ambienteBO = new AmbienteBO(conexao.criarConexaoSAAS(this.estagio));
        Ambiente ambiente = ambienteBO.carregarAmbientePorChaveOrganizacao(this.chaveOrganizacao);
        this.con = conexao.criarConexaoAmbiente(ambiente);
        return this.con;
    }

}
