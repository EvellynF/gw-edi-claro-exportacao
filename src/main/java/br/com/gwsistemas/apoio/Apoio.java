package br.com.gwsistemas.apoio;

import br.com.gwsistemas.configuracao.ConfiguracaoSAAS;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apoio {
    private static Logger log = LoggerFactory.getLogger(Apoio.class);
    private static final Algorithm algorithm = Algorithm.HMAC256(System.getenv("CHAVE_JWT"));
    private static final JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("gwsistemas").build();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @NonNull
    public static ConfiguracaoSAAS carregarConfiguracaoSAAS(String estagio) {
        ConfiguracaoSAAS configuracaoSAAS = new ConfiguracaoSAAS();

        configuracaoSAAS.setTempoSessao(Integer.parseInt(System.getenv("TEMPO_SESSAO")));
        configuracaoSAAS.setBancoHost(System.getenv("DB_HOST_" + estagio));
        configuracaoSAAS.setBancoUsuario(System.getenv("DB_USUARIO_" + estagio));
        configuracaoSAAS.setBancoSenha(System.getenv("DB_SENHA_" + estagio));
        configuracaoSAAS.setBancoPorta(Integer.parseInt(System.getenv("DB_PORTA_" + estagio)));
        configuracaoSAAS.setBancoNome(System.getenv("DB_NOME_" + estagio));

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
}
