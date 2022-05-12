package br.com.gwsistemas.exportacaoEdiClaro;

import br.com.gwsistemas.apoio.Apoio;
import br.com.gwsistemas.claro.bean.ArrayOfCTEV2;
import br.com.gwsistemas.claro.bean.ArrayOfDocumentoFiscalV2;
import br.com.gwsistemas.claro.bean.ArrayOfOcorrenciaTransporteV2;
import br.com.gwsistemas.claro.bean.CTEV2;
import br.com.gwsistemas.claro.bean.DocumentoFiscalV2;
import br.com.gwsistemas.claro.bean.EnumTipoDocumento;
import br.com.gwsistemas.claro.bean.OcorrenciaTransporteV2;
import br.com.gwsistemas.cliente.Cliente;
import br.com.gwsistemas.cliente.ClienteLayoutEDI;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.datatype.DatatypeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportacaoEdiClaroDAO {

    private static Logger log = LoggerFactory.getLogger(ExportacaoEdiClaroDAO.class);
    private Connection connection = null;

    public ExportacaoEdiClaroDAO(Connection con) {
            this.connection = con;
    }

    public ArrayList<Cliente> obterDadosAutorizacao(int idConsignatario, String dataInicio, String dataFim, String ids, String layoutCliente) throws SQLException, ParseException {
        ArrayList<Cliente> clientes = new ArrayList<>();
        Cliente cliente = null;
        ClienteLayoutEDI layout = null;
        StringBuilder sql;
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        sql = new StringBuilder();
        try {
            sql.append(" SELECT DISTINCT ON (cl.idcliente) cl.razaosocial, cl.cnpj, cle.login_webservice, cle.senha_webservice, cle.chave_webservice ");
            sql.append(" from protocolo_autorizacao_cte pac ");
            sql.append(" JOIN sales sl ON (sl.id = pac.ctrc_id and categoria = 'ct') JOIN cliente cl ON (sl.consignatario_id = cl.idcliente) JOIN cliente_layout_edi cle ON (sl.consignatario_id = cle.cliente_id) ");
            if (ids != null) {
                sql.append(" where ctrc_id IN (").append(ids).append(") ");
            } else {
                sql.append(" where sl.emissao_em BETWEEN ? and ? and sl.consignatario_id =").append(idConsignatario);
            }
            sql.append(" and evento is null and cle.layout_formato_antigo = ").append(Apoio.SqlFix(layoutCliente));

            prepSt = connection.prepareStatement(sql.toString());
            if (ids == null) {
                prepSt.setDate(1, Apoio.getFormatSqlData(dataInicio));
                prepSt.setDate(2, Apoio.getFormatSqlData(dataFim));
            }
            rs = prepSt.executeQuery();

            while (rs.next()) {
                cliente = new Cliente();
                cliente.setRazaosocial(rs.getString("razaosocial"));
                cliente.setCnpj(rs.getString("cnpj"));
                layout = new ClienteLayoutEDI("c", cliente);
                layout.setLogin(rs.getString("login_webservice"));
                layout.setSenha(rs.getString("senha_webservice"));
                layout.setChave(rs.getString("chave_webservice"));
                cliente.getLayoutsCONEMB().add(layout);
                clientes.add(cliente);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (prepSt != null && !prepSt.isClosed()) {
                prepSt.close();
                prepSt = null;
            }

            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }
            
        }
        return clientes;
    }

    public ArrayOfDocumentoFiscalV2 montarConembCte(int idConsignatario, String dataInicio, String dataFim, String ids) throws SQLException {
        ArrayOfDocumentoFiscalV2 paramDOC = null;
        DocumentoFiscalV2 doc;
        PreparedStatement prepst2 = null;
        ResultSet rs2 = null;
        DocumentoFiscalV2.ItensDocumentosFiscais idoc;
        DocumentoFiscalV2.NotasFiscais nf;
        DocumentoFiscalV2.Composicao composicao;
        StringBuilder sqlNotas = null;
        StringBuilder sql = new StringBuilder();
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        try {

            sql.append(" SELECT COALESCE(serie_redespacho, '') serie_redespacho, COALESCE(redespacho_ctrc , '') numero_redespacho, ");
            sql.append(" COALESCE(sl.redespacho_ctrc , '') numero_redespacho , COALESCE(red.cnpj,'') redespacho_cgc, * from vexport_edi_conemb v ");
            sql.append(" LEFT join ctrcs sl ON (sl.sale_id = v.idmovimento) LEFT JOIN cliente red ON (red.idcliente = sl.redespacho_id) ");

            if (ids != null) {
                sql.append(" where idmovimento IN (").append(ids).append(") ");
            } else {
                sql.append(" where ct_emissao BETWEEN ? AND ? and cliente_id = ").append(idConsignatario);
            }

            prepSt = connection.prepareStatement(sql.toString());

            if (ids == null) {
                prepSt.setDate(1, Apoio.getFormatSqlData(dataInicio));
                prepSt.setDate(2, Apoio.getFormatSqlData(dataFim));
            }
            paramDOC = new ArrayOfDocumentoFiscalV2();
            rs = prepSt.executeQuery();
            while (rs.next()) {
                doc = new DocumentoFiscalV2();

                doc.setCNPJContratanteTransporte(rs.getString("cl_cgc"));
                doc.setRazaoSocial(rs.getString("razao_filial"));
                doc.setFilialEmissor(rs.getString("fi_abreviatura"));
                doc.setSerie(rs.getString("ct_serie"));
                doc.setNumero(rs.getString("ct_ctrc"));
                doc.setKey(rs.getString("chave_acesso_cte"));
                doc.setDataEmissao(Apoio.converterDataToXMLGregorianCalendar(rs.getDate("ct_emissao"), rs.getDate("ct_emissao_as")));
                doc.setCondicaoFrete(rs.getString("ct_tipofrete"));
                doc.setPesoTransportado(rs.getBigDecimal("ct_peso"));
                doc.setValorDocumento(rs.getBigDecimal("total_receita"));
                doc.setBaseCalculoApuracaoICMS(rs.getBigDecimal("ct_baseicms"));
                doc.setTaxaICMS(rs.getBigDecimal("ct_aliqicms"));
                doc.setValorICMS(rs.getBigDecimal("ct_vlicms"));
                doc.setValorISS(rs.getBigDecimal("valor_iss"));
                doc.setValorFretePeso(rs.getBigDecimal("valor_peso"));
                doc.setValorFreteTaxa(rs.getBigDecimal("valor_frete"));
                doc.setSubstituicaoTributaria(false);
                doc.setCNPJEmissorConhecimento(rs.getString("fi_cnpj"));
                doc.setCNPJCPFEmbarcador(rs.getString("rem_cgc"));
                doc.setCNPJCPFDestinatario(rs.getString("des_cgc"));
                doc.setTipoDocumento(EnumTipoDocumento.CTE);

                if ("CTE".equalsIgnoreCase(rs.getString("tipo_nc"))) {
                    doc.setTipoDocumento(EnumTipoDocumento.CTE);
                } else {
                    doc.setTipoDocumento(EnumTipoDocumento.NF_SE);
                }

                doc.setTipoMeioTransporte("");
                doc.setFilialEmissoraConhecimentoOriginador(rs.getString("redespacho_cgc"));
                doc.setSerieConhecimentoOriginador(rs.getString("serie_redespacho"));
                doc.setNumeroConhecimentoOriginador(rs.getString("numero_redespacho"));
                doc.setCodigoSolicitacaoColeta("");
                
                BigDecimal zero = new BigDecimal(0);
                if(rs.getBigDecimal("valor_peso") != zero){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(1);
                    composicao.setDescricaoServico("FRETE-PESO");
                    composicao.setValorServico(rs.getBigDecimal("valor_peso"));
                    doc.getComposicao().add(composicao);
                }
                if(rs.getBigDecimal("valor_gris") != zero){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(10);
                    composicao.setDescricaoServico("GRIS");
                    composicao.setValorServico(rs.getBigDecimal("valor_gris"));
                    doc.getComposicao().add(composicao);
                }
                if(rs.getBigDecimal("valor_frete") != zero){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(11);
                    composicao.setDescricaoServico("AD VALOREN");
                    composicao.setValorServico(rs.getBigDecimal("valor_frete"));
                    doc.getComposicao().add(composicao);
                }
                if(rs.getBigDecimal("valor_frete") != zero){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(21);
                    composicao.setDescricaoServico("REENTREGA");
                    composicao.setValorServico(rs.getBigDecimal("valor_frete"));
                    doc.getComposicao().add(composicao);
                }
                if("r".equals(rs.getString("sl_tipo_ctrc"))){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(21);
                    composicao.setDescricaoServico("REENTREGA");
                    composicao.setValorServico(rs.getBigDecimal("valor_peso"));
                    doc.getComposicao().add(composicao);
                }
                if("d".equals(rs.getString("sl_tipo_ctrc"))){
                    composicao = new DocumentoFiscalV2.Composicao();
                    composicao.setCodigoServico(21);
                    composicao.setDescricaoServico("DEVOLUÇÃO");
                    composicao.setValorServico(rs.getBigDecimal("valor_peso"));
                    doc.getComposicao().add(composicao);
                }

                sqlNotas = new StringBuilder("SELECT nf.numero , nf.serie, nf.chave_acesso FROM vexport_edi_conemb v JOIN nota_fiscal nf ON (nf.idconhecimento = v.idmovimento)");
                if (ids != null) {
                    sqlNotas.append(" WHERE idmovimento IN (").append(ids).append(") ");
                } else {
                    sqlNotas.append(" WHERE ct_emissao BETWEEN ? AND ? and cliente_id = ").append(idConsignatario);
                }

                prepst2 = connection.prepareStatement(sqlNotas.toString());
                if (ids == null) {
                    prepst2.setDate(1, Apoio.getFormatSqlData(dataInicio));
                    prepst2.setDate(2, Apoio.getFormatSqlData(dataFim));
                }
                rs2 = prepst2.executeQuery();
                while (rs2.next()) {
                    nf = new DocumentoFiscalV2.NotasFiscais();
                    nf.setKeyNotaFiscal(rs2.getString("chave_acesso"));
                    nf.setNumeroNotaFiscal(rs2.getString("numero"));
                    nf.setSerieNotaFiscal(rs2.getString("serie"));
                    doc.getNotasFiscais().add(nf);
                }
                sqlNotas.delete(0, sqlNotas.length());

                sqlNotas = new StringBuilder(" SELECT COALESCE(oc.ocorrencia_em, sl.baixa_em) as data_ocorrencia, ");
                sqlNotas.append("  COALESCE(oc.ocorrencia_as,ocnf.ocorrencia_as, '0000') ocorrencia_as, COALESCE(oct.codigo,onf.codigo) cod_ocorrencia, ");
                sqlNotas.append(" COALESCE(oct.descricao,onf.descricao) desc_ocorrencia ");
                sqlNotas.append(" FROM vexport_edi_conemb v JOIN nota_fiscal nf ON (nf.idconhecimento = v.idmovimento)  JOIN ctrcs sl ON (sl.sale_id = v.idmovimento) ");
                sqlNotas.append(" LEFT JOIN ocorrencias oc ON (oc.sale_id = v.idmovimento) LEFT JOIN ocorrencia_ctrcs oct ON (oct.id = sl.ocorrencia_id) ");
                sqlNotas.append(" LEFT JOIN ocorrencias ocnf ON (oc.nota_fiscal_id = nf.idnota_fiscal) LEFT JOIN ocorrencia_ctrcs onf ON (onf.id = nf.ocorrencia_id) ");
                if (ids != null) {
                    sqlNotas.append(" WHERE idmovimento IN (").append(ids).append(") ");
                } else {
                    sqlNotas.append(" WHERE ct_emissao BETWEEN ? AND ? and cliente_id = ").append(idConsignatario);
                }

                prepst2 = connection.prepareStatement(sqlNotas.toString());
                if (ids == null) {
                    prepst2.setDate(1, Apoio.getFormatSqlData(dataInicio));
                    prepst2.setDate(2, Apoio.getFormatSqlData(dataFim));
                }
                rs2 = prepst2.executeQuery();
                while (rs2.next()) {
                    idoc = new DocumentoFiscalV2.ItensDocumentosFiscais();
                    idoc.setCodigoOcorrencia(rs2.getString("cod_ocorrencia"));
                    idoc.setDataOcorrencia(Apoio.converterDataToGregorianCalendarFusoHorario(rs2.getDate("data_ocorrencia"), rs2.getDate("ocorrencia_as")));
                    idoc.setDescricao(rs2.getString("desc_ocorrencia"));

                    doc.getItensDocumentosFiscais().add(idoc);
                }
                paramDOC.getDocumentoFiscalV2().add(doc);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            
            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }

            if (prepst2 != null && !prepst2.isClosed()) {
                prepst2.close();
                prepst2 = null;
            }

            if (rs2 != null && !rs2.isClosed()) {
                rs2.close();
                rs2 = null;
            }
        }
        return paramDOC;
    }

    public ArrayOfOcorrenciaTransporteV2 montarOcorenCte(int idConsignatario, String dataInicio, String dataFim, String ids) throws SQLException, ParseException, DatatypeConfigurationException {
            ArrayOfOcorrenciaTransporteV2 paramDOC = null;
            OcorrenciaTransporteV2 oco;
            PreparedStatement prepst2 = null;
            ResultSet rs2 = null;
            OcorrenciaTransporteV2.NotasFiscais nf;
            StringBuilder sqlNotas = null;
            StringBuilder sql = null;
            PreparedStatement prepSt = null;
            ResultSet rs = null;
        try {
            sql = new StringBuilder();
            sql.append(" SELECT * from vexport_edi_ocoren v ");
            if (ids != null) {
                sql.append(" where sale_id IN (").append(ids).append(") ");
            } else {
                sql.append(" where dtemissao BETWEEN ? AND ? and cliente_id = ").append(idConsignatario);
            }

            prepSt = connection.prepareStatement(sql.toString());

            if (ids == null) {
                prepSt.setDate(1, Apoio.getFormatSqlData(dataInicio));
                prepSt.setDate(2, Apoio.getFormatSqlData(dataFim));
            }

            paramDOC = new ArrayOfOcorrenciaTransporteV2();
            rs = prepSt.executeQuery();

            while (rs.next()) {
                oco = new OcorrenciaTransporteV2();
                oco.setCNPJContratanteTransporte(rs.getString("cl_cgc"));
                oco.setIdentificacaoDocumento("NFE");
                oco.setRazaoSocial(rs.getString("razao_filial"));
                oco.setCNPJCPFEmissorNotaFiscal(rs.getString("cgc_remetente"));
                oco.setCNPJCPFDestinatario(rs.getString("cgc_destinatario"));
                oco.setCodigoOcorrencia(rs.getString("codigo_ocorrencia_ctrc"));
                oco.setDataHoraOcorrencia(Apoio.converterDataToGregorianCalendarFusoHorario(
                                rs.getDate("data_ocorrencia"), Apoio.getFormatTime(ids)));
                oco.setNomeRecebedor("");
                oco.setDocumentoRecebedor("");
                nf = new OcorrenciaTransporteV2.NotasFiscais();
                nf.setKeyNotaFiscal(rs.getString("chave_acesso_nota"));
                nf.setNumeroNotaFiscal(rs.getString("nf_numero"));
                nf.setSerieNotaFiscal(rs.getString("nf_serie"));
                oco.getNotasFiscais().add(nf);
                
                
                paramDOC.getOcorrenciaTransporteV2().add(oco);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            
            if (prepSt != null && !prepSt.isClosed()) {
                prepSt.close();
                prepSt = null;
            }
            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }
            if (prepst2 != null && !prepst2.isClosed()) {
                prepst2.close();
                prepst2 = null;
            }
            if (rs2 != null && !rs2.isClosed()) {
                rs2.close();
                rs2 = null;
            }
        }
        return paramDOC;
    }

    public ArrayOfCTEV2 montarArrayCte(int idConsignatario, String dataInicio, String dataFim, String ids) throws SQLException, ParseException, DatatypeConfigurationException {
        ArrayOfCTEV2 paramCTE = null;
        CTEV2 cte;
        StringBuilder sql = null;
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        try {
            sql = new StringBuilder();

            sql.append("select distinct on (ctrc_id) xml_autorizacao , data_hora_autorizacao, ct.chave_acesso_cte ");
            sql.append("from protocolo_autorizacao_cte pac ");
            sql.append("JOIN sales sl ON (sl.id = pac.ctrc_id) JOIN ctrcs ct ON (sl.id = ct.sale_id) ");
            
            if (ids != null) {
                sql.append(" where ctrc_id IN (").append(ids).append(") ");
            } else {
                sql.append(" where sl.emissao_em BETWEEN ? and ? ");
            }
            
            sql.append(" and sl.consignatario_id =").append(idConsignatario);
            sql.append(" and evento is null order by ctrc_id, data_hora_autorizacao desc; ");

            prepSt = connection.prepareStatement(sql.toString());

            if (ids == null) {
                prepSt.setDate(1, Apoio.getFormatSqlData(dataInicio));
                prepSt.setDate(2, Apoio.getFormatSqlData(dataFim));
            }
            
            paramCTE = new ArrayOfCTEV2();
            rs = prepSt.executeQuery();
            
            while (rs.next()) {
                cte = new CTEV2();
                cte.setDataRemessa(Apoio.converterDataToXMLGregorianCalendar(rs.getDate("data_hora_autorizacao"),
                                rs.getDate("data_hora_autorizacao")));
                cte.setNumeroChaveConhecimento(rs.getString("chave_acesso_cte"));
                cte.setTipoDocumento(EnumTipoDocumento.CTE);
                cte.setXmlCTE(new String(rs.getBytes("xml_autorizacao")));
                paramCTE.getCTEV2().add(cte);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (prepSt != null && !prepSt.isClosed()) {
                prepSt.close();
                prepSt = null;
            }
            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }
        }
        return paramCTE;
    }

}
