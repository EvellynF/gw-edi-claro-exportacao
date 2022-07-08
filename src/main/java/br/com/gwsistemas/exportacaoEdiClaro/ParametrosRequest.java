
package br.com.gwsistemas.exportacaoEdiClaro;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Value
public class ParametrosRequest {
    
    @NotBlank(message = "O C�digo da organiza��o n�o pode ficar em branco.")
    private String chaveOrganizacao;
    @NotBlank
    private String dtinicialedi;
    @NotBlank
    private String dtfinaledi;
    private int idconsignatario;
    @NotBlank
    private String ids;
    @NotBlank
    private String estagio; 
}
