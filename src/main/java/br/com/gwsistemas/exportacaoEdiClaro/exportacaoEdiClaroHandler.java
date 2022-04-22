package br.com.gwsistemas.exportacaoEdiClaro;

import br.com.gwsistemas.apoio.Apoio;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.util.Collections;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class exportacaoEdiClaroHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private static Logger log = LoggerFactory.getLogger(exportacaoEdiClaroHandler.class);
    
    private static final String EXPORTAR_CLARO_XML = "exportarClaroXML";
    private static final String EXPORTAR_CLARO_CONEMB = "exportarClaroConemb";
    private static final String EXPORTAR_CLARO_OCOREN = "webserviceClaroOcoren";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        
        
        log.debug("evento={}", requestEvent);

        APIGatewayProxyResponseEvent responseEvent = null;
        ParametrosRequest parametros = null;
        
        try{

            boolean tokenValido = Apoio.validarTokenJwt(requestEvent.getHeaders().get("token"));
            
            if(!tokenValido){
                responseEvent = new APIGatewayProxyResponseEvent();

                responseEvent.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
                responseEvent.setBody(Apoio.OBJECT_MAPPER.writeValueAsString(Collections.singletonMap("erro", "Token inválido.")));

                return responseEvent;
            }
            
            //if("POST".equals(requestEvent.getHttpMethod())){
                parametros = Apoio.OBJECT_MAPPER.readValue(requestEvent.getBody(),  ParametrosRequest.class);
                return exportarClaro(parametros);

           // }
            
            //responseEvent = new APIGatewayProxyResponseEvent();
            //responseEvent.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            //responseEvent.setHeaders("","");
            //return responseEvent;
            
        } catch(Exception ex){

            log.error(ex.getMessage(), ex);

            responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            responseEvent.setBody(String.format("Erro interno: %s", ex.getMessage()));

            return responseEvent;
        } 
    }
    
    private APIGatewayProxyResponseEvent exportarClaro(ParametrosRequest parametros) {
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
        } finally {
            bo = null;
            dataInicio = null;
            dataFim = null;
        }
        return responseEvent;
    }
    
}
