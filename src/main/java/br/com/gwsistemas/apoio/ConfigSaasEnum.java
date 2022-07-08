
package br.com.gwsistemas.apoio;

public enum ConfigSaasEnum {
    DEV("https://api.gwsistemas.com.br/api-gwsistemas-dev","DEV"),
    HOM("https://api.gwsistemas.com.br/api-gwsistemas-hom","HOM"),
    PROD("https://api.gwsistemas.com.br/api-gwsistemas-prod","PROD");
    
    private final String urlApi;
    private final String estagio;

    ConfigSaasEnum(String urlApi, String estagio) {
        this.urlApi = urlApi;
        this.estagio = estagio;
    }
    
    public String getEstagio(){
        return estagio;
    }
    
    public String getUrlApi(){
        return urlApi;
    }
}
