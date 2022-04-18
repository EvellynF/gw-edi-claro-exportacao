package br.com.gwsistemas.cliente;

import java.util.Collection;

import lombok.Data;

@Data
public class Cliente {

	private String razaosocial = "";
	private String cnpj = "";

	private Collection<ClienteLayoutEDI> layoutsCONEMB = null;

}
