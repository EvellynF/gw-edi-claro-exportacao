package br.com.gwsistemas.apoio;

import br.com.gwsistemas.configuracao.ConfiguracaoSAAS;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import br.com.gwsistemas.apoio.ConfigSaasEnum;

import org.apache.commons.io.IOUtils;

@Slf4j
public class Apoio {
    //private static final Algorithm algorithm = Algorithm.HMAC256(System.getenv("CHAVE_JWT"));
    private static final Algorithm algorithm = Algorithm.HMAC256("OYt_KSxiFK7x_7f5GuSfzmmXwqiLcj_vFx2I3G7R");
    private static final JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("gwsistemas").build();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static String LINK_GWSAAS = "";
    
    public static void setLINK_GWSAAS(String estagio) {
    	ConfigSaasEnum config = ("DEV".equals(estagio) ? ConfigSaasEnum.DEV : ("PROD".equals(estagio) ? ConfigSaasEnum.PROD : ConfigSaasEnum.HOM));
        Apoio.LINK_GWSAAS = config.getUrlApi();
    }

    @NonNull
    public static ConfiguracaoSAAS carregarConfiguracaoSAAS(String estagio) {
        ConfiguracaoSAAS configuracaoSAAS = new ConfiguracaoSAAS();

//        configuracaoSAAS.setTempoSessao(Integer.parseInt(System.getenv("TEMPO_SESSAO")));
//        configuracaoSAAS.setBancoHost(System.getenv("DB_HOST_" + estagio));
//        configuracaoSAAS.setBancoUsuario(System.getenv("DB_USUARIO_" + estagio));
//        configuracaoSAAS.setBancoSenha(System.getenv("DB_SENHA_" + estagio));
//        configuracaoSAAS.setBancoPorta(Integer.parseInt(System.getenv("DB_PORTA_" + estagio)));
//        configuracaoSAAS.setBancoNome(System.getenv("DB_NOME_" + estagio));

        configuracaoSAAS.setTempoSessao(120);
        configuracaoSAAS.setBancoHost("ls-7eb17fe5d983c24c634d0202c1e7dde6ba3b3882.cmvwgqf6uoyn.us-east-2.rds.amazonaws.com");
        configuracaoSAAS.setBancoUsuario("dbmasteruser");
        configuracaoSAAS.setBancoSenha("b12alpha1");
        configuracaoSAAS.setBancoPorta(5432);
        configuracaoSAAS.setBancoNome("gw_portal_saas_dev");
        return configuracaoSAAS;
    }
    
    public static boolean validarTokenJwt(String tokenJwt) {
        try {
            if (tokenJwt == null || "".equals(tokenJwt)) {
                return false;
            }

            jwtVerifier.verify(tokenJwt);
            
            return true;
        } catch (JWTVerificationException ex) {
            log.error(ex.getMessage(), ex);
        }
        
        return false;
    }
    
    public static java.sql.Date getFormatSqlData(String data) throws ParseException {
        // Este Metodo Converte String em sql.Date
        java.sql.Date retorno = null;
        if (data == null || data.trim().equalsIgnoreCase("")) {
            retorno = null;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            retorno = new java.sql.Date(dateFormat.parse(data).getTime());
        }
        return retorno;

    }
    
    //Converter data em XMLGregorianCalendar
    public static XMLGregorianCalendar converterDataToXMLGregorianCalendar(Date data, Date hora) throws DatatypeConfigurationException, ParseException {

        SimpleDateFormat formatoData = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String dataString = formatoData.format(data);
        String horaString = formatoHora.format(hora);

        String dataHoraString = dataString + " " + horaString;

        Date DataHora = (Date) formatoDataHora.parse(dataHoraString);

        GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        gregorianCalendar.setTime(DataHora);

        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar.get(Calendar.YEAR), gregorianCalendar.get(Calendar.MONTH) + 1,
                gregorianCalendar.get(Calendar.DAY_OF_MONTH), gregorianCalendar.get(Calendar.HOUR_OF_DAY), gregorianCalendar.get(Calendar.MINUTE), gregorianCalendar.get(Calendar.SECOND), DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
    }
    
    public static XMLGregorianCalendar converterDataToGregorianCalendarFusoHorario(Date data, Date hora) throws DatatypeConfigurationException, ParseException {

        SimpleDateFormat formatoData = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
        String dataString = formatoData.format(data);
        String horaString = formatoHora.format(hora);

        String dataHoraString = dataString + " " + horaString;

        Date DataHora = (Date) formatoDataHora.parse(dataHoraString);

        GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        gregorianCalendar.setTime(DataHora);

        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }
    
    public static String SqlFix(final String Valor) {   //se o valor estiver nulo, retorne nulo
        if (Valor == null) {
            return null;
        }

        int length = Valor.length();
        StringBuilder valorfixo = new StringBuilder((int) (length * 1.1));

        for (int i = 0; i < length; i++) {
            char c = Valor.charAt(i);
            if (c == '\'') {
                valorfixo.append("\"");
            } else if (c == '\\') {
                valorfixo.append("\\\\");
            } //valorfixo.append("/");
            else if (c == '>') {
            } else if (c == '<') {
            } else if (c == '"') {
            } else {
                valorfixo.append(c);
            }
        }

        return "'" + valorfixo.toString().trim() + "'";
    }
    
    public static Date getFormatTime(String hora) throws ParseException {
        // Este Metodo Converte String em Date
        Date retorno = null;
        if (hora == null || hora.trim().equalsIgnoreCase("")) {
            retorno = null;
        } else {
            String barra1 = String.valueOf(hora.charAt(2));
            if (barra1.equals(":")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                retorno = (Date) dateFormat.parse(hora);
            }
        }
        return retorno;
    }
    
    @SneakyThrows
    public static String gerarLinkPreAssinado(String caminhoAnexo, String rotina, String token) {
        String postURL = null;
        HttpPost post = null;
        JSONObject payload = null;
        String retorno = null;
        Map<String, String> retornoMap = null;

        try {
            postURL = Apoio.LINK_GWSAAS + "/upload-s3/organizacao/gerar-link";
            post = new HttpPost(postURL);

            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("Token", token);

            payload = new JSONObject();
            payload.put("rotina", rotina);
            payload.put("caminho_anexo", caminhoAnexo);
            post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

            if (log.isDebugEnabled()) {
                log.debug("JSON ENVIO=" + EntityUtils.toString(post.getEntity()));
            }

            retorno = executarRequisicaoPost(post);
            retornoMap = new Gson().fromJson(retorno, new TypeToken<Map<String, String>>() {
            }.getType());

            return retornoMap.get("link");
        } catch (JSONException | IOException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            postURL = null;
            post = null;
            payload = null;
            retorno = null;
        }

        return null;
    }
    
    private static String executarRequisicaoPost(HttpPost post) throws IOException {
        HttpResponse responsePOST = null;
        String response = null;

        try {
            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                responsePOST = client.execute(post);

                response = IOUtils.toString(responsePOST.getEntity().getContent(), StandardCharsets.UTF_8);

                if (responsePOST.getStatusLine().getStatusCode() != HttpStatus.SC_OK && responsePOST.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                    throw new IOException(response);
                }

                return response;
            }
        } finally {
            responsePOST = null;
            response = null;
        }
    }
}
