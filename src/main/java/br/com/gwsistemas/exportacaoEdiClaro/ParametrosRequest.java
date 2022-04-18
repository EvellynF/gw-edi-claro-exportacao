
package br.com.gwsistemas.exportacaoEdiClaro;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class ParametrosRequest {
    
    @NotNull(message = "O Código da organização não pode ficar em branco.")
    @Size(min = 1, message = "O Código da organização não pode ficar em branco.")
    private String chaveOrganizacao;
    @NotNull
    private String dtinicialedi;
    @NotNull
    private String dtfinaledi;
    @NotNull
    private String acao;
    @NotNull
    private int idconsignatario;
    @NotNull
    private String ids;
    @NotNull
    private String estagio; 
}
