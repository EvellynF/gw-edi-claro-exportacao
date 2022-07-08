package br.com.gwsistemas.exportacaoEdiClaro;

import br.com.gwsistemas.apoio.Apoio;
import br.com.gwsistemas.apoio.ValidaBean;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Collections;
import org.apache.http.HttpStatus;

@Slf4j
public class exportacaoEdiClaroHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
        
    @Override @SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        APIGatewayProxyResponseEvent responseEvent = null;
        ParametrosRequest parametros = null;
        String enviarXmlClaro = "";
        ExportacaoEdiClaroBO bo = null;
        
        try{
        	boolean tokenValido = Apoio.validarTokenJwt(requestEvent.getHeaders().get("token"));            
            if(!tokenValido){
                responseEvent = new APIGatewayProxyResponseEvent();
                responseEvent.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
                responseEvent.setBody(Apoio.OBJECT_MAPPER.writeValueAsString(Collections.singletonMap("erro", "Token inválido.")));
                return responseEvent;
            }
            
            if("POST".equals(requestEvent.getHttpMethod())) {
            	
            	parametros = Apoio.OBJECT_MAPPER.readValue(requestEvent.getBody(),  ParametrosRequest.class);
                String erros = ValidaBean.valid(parametros);
                if (!erros.isEmpty()){
                    responseEvent = new APIGatewayProxyResponseEvent();
                    responseEvent.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    responseEvent.setBody(String.format("Erro: %s", erros));
                    return responseEvent;
                }
                Apoio.setLINK_GWSAAS(parametros.getEstagio());
                
            	switch(requestEvent.getPath()) {
            		case "/exportarClaroXML":
            			bo = new ExportacaoEdiClaroBO(parametros.getChaveOrganizacao(), parametros.getEstagio());
            			enviarXmlClaro = bo.enviarXmlClaro(parametros.getIdconsignatario(), parametros.getDtinicialedi(), parametros.getDtfinaledi(), parametros.getIds()).toString();
                        break;
            		case "/exportarClaroConemb":
            			bo = new ExportacaoEdiClaroBO(parametros.getChaveOrganizacao(), parametros.getEstagio());
            			enviarXmlClaro = bo.enviarConembClaro(parametros.getIdconsignatario(), parametros.getDtinicialedi(), parametros.getDtfinaledi(), parametros.getIds()).toString();
                        break;
            		case "/webserviceClaroOcoren":
            			bo = new ExportacaoEdiClaroBO(parametros.getChaveOrganizacao(), parametros.getEstagio());
            			enviarXmlClaro = bo.enviarOcorenClaro(parametros.getIdconsignatario(), 
            					parametros.getDtinicialedi(), 
            					parametros.getDtfinaledi(), 
            					parametros.getIds(),
            					requestEvent.getHeaders().get("token")
            					).toString();
                        break;
                    default:
                    	responseEvent = new APIGatewayProxyResponseEvent();
                        responseEvent.setStatusCode(HttpStatus.SC_NOT_FOUND);
                        return responseEvent;
            	}
            	responseEvent = new APIGatewayProxyResponseEvent();
                responseEvent.setStatusCode(HttpStatus.SC_OK);
                responseEvent.setBody(enviarXmlClaro);
                return responseEvent;
            }
            
            responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            return responseEvent;
        } catch(Exception ex){

            log.error(ex.getMessage(), ex);

            responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            responseEvent.setBody(String.format("Erro interno: %s", ex.getMessage()));
            return responseEvent;
        } 
    }
    
//    @SneakyThrows
//    public static void main(String[] args) throws JsonMappingException, JsonProcessingException{
//        String body = "{\"chaveOrganizacao\":\"GWSISTEMASDEV\",\"dtinicialedi\":\"16/06/2022\",\"dtfinaledi\":\"16/06/2022\",\"idconsignatario\":6930,\"ids\":\"51428\",\"estagio\":\"DEV\"}";
//        System.out.println(body);
//        ParametrosRequest parametros = Apoio.OBJECT_MAPPER.readValue(body,  ParametrosRequest.class);
//        
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBdXRlbnRpY2FkbyI6eyJpZCI6MzMsIm5vbWUiOiJTdXBvcnRlIiwic29icmVfbm9tZSI6IlRlY25pY28iLCJhcGVsaWRvIjoiU3Vwb3J0ZSBUZWNuaWNvIiwiZGF0YV9uYXNjaW1lbnRvIjoiMTExMS0xMS0xMVQwMDowMDowMFoiLCJlbWFpbCI6Imd3c2lzdGVtYXMuYWdlbmRhQGdtYWlsLmNvbSIsImF0aXZvIjp0cnVlLCJndWlkIjoiMDQ0NzFmZmQtZGY0OC00MDNjLThkYWMtN2IxYmRlNmE4MzE0In0sImV4cCI6MTY1NzAyNzc4OCwiaXNzIjoiZ3dzaXN0ZW1hcyJ9.NAMo6Ihmtz_iW9Mcr4EI0IRj_R1w_fsTCuaRtL9LI4E";
//        
//        ExportacaoEdiClaroBO bo = new ExportacaoEdiClaroBO(parametros.getChaveOrganizacao(), parametros.getEstagio());
//        String enviarXmlClaro = bo.enviarOcorenClaro(parametros.getIdconsignatario(), 
//				parametros.getDtinicialedi(), 
//				parametros.getDtfinaledi(), 
//				parametros.getIds(),
//				token
//				).toString();
//        System.out.println("\n\nenviarXmlClaro: "+enviarXmlClaro+"\n");
//        //exportarClaro(parametros);
//    }
    
}
