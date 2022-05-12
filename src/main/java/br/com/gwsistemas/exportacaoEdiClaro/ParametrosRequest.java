
package br.com.gwsistemas.exportacaoEdiClaro;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParametrosRequest {
    
    @NotBlank(message = "O C�digo da organiza��o n�o pode ficar em branco.")
    private String chaveOrganizacao;
    @NotBlank
    private String dtinicialedi;
    @NotBlank
    private String dtfinaledi;
    @NotBlank
    private String acao;
    private int idconsignatario;
    @NotBlank
    private String ids;
    @NotBlank
    private String estagio; 
}
