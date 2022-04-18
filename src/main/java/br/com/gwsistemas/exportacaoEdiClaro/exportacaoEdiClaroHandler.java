package br.com.gwsistemas.exportacaoEdiClaro;

import br.com.gwsistemas.apoio.Apoio;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

@Slf4j
public class exportacaoEdiClaroHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final String EXPORTAR_CLARO_XML = "exportarClaroXML";
    private static final String EXPORTAR_CLARO_CONEMB = "exportarClaroConemb";
    private static final String EXPORTAR_CLARO_OCOREN = "webserviceClaroOcoren";

    @SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        APIGatewayProxyResponseEvent responseEvent = null;
        ParametrosRequest parametros = null;
        
        try{

            boolean tokenValido = Apoio.validarTokenJwt(input.getHeaders().get("token"));
            
            if(!tokenValido){
                responseEvent = new APIGatewayProxyResponseEvent();

                responseEvent.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
                responseEvent.setBody(Apoio.OBJECT_MAPPER.writeValueAsString(Collections.singletonMap("erro", "Token inválido.")));

                return responseEvent;
            }
            
            switch(input.getHttpMethod().toUpperCase()){
                case "POST":
                    parametros = Apoio.OBJECT_MAPPER.readValue(input.getBody(),  ParametrosRequest.class);
                    return exportarClaro(parametros);
                default:
                    responseEvent = new APIGatewayProxyResponseEvent();
                    responseEvent.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                    //responseEvent.setHeaders("","");
                    return responseEvent;
                    
            }
        } catch(Exception ex){

            log.error(ex.getMessage(), ex);

            responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            responseEvent.setBody(Apoio.OBJECT_MAPPER.writeValueAsString(Collections.singletonMap("erro", String.format("Erro interno: %s", ex.getMessage()))));

            return responseEvent;
        } 
    }
    
    private APIGatewayProxyResponseEvent exportarClaro(ParametrosRequest parametros) throws Exception {
        ExportacaoEdiClaroBO bo;
        String dataInicio = null;
        String dataFim = null;
        String enviarXmlClaro = null;
        String acao = null;
        APIGatewayProxyResponseEvent responseEvent = null;

        try {
            dataInicio = parametros.getDtinicialedi();
            dataFim = parametros.getDtfinaledi();
            acao = parametros.getAcao();
            bo = new ExportacaoEdiClaroBO(parametros.getChaveOrganizacao(), parametros.getEstagio());
            switch(acao){
                case EXPORTAR_CLARO_XML:
                    enviarXmlClaro = bo.enviarXmlClaro(parametros.getIdconsignatario(), dataInicio, dataFim, parametros.getIds(), parametros.getChaveOrganizacao()).toString();
                    break;
                case EXPORTAR_CLARO_OCOREN: 
                    enviarXmlClaro = bo.enviarOcorenClaro(parametros.getIdconsignatario(), dataInicio, dataFim, parametros.getIds()).toString();
                    break;
                case EXPORTAR_CLARO_CONEMB:
                    enviarXmlClaro = bo.enviarConembClaro(parametros.getIdconsignatario(), dataInicio, dataFim, parametros.getIds()).toString();
                    break;
                default:
                    
            }
            
            responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_OK);
            responseEvent.setBody(enviarXmlClaro);

        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            throw ex;
        } finally {
            bo = null;
            dataInicio = null;
            dataFim = null;
        }
        return responseEvent;
    }
    
}
