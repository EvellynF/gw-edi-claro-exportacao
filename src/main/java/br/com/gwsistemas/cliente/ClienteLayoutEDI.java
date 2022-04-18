package br.com.gwsistemas.cliente;

import br.com.gwsistemas.cliente.Cliente;
import lombok.Data;

@Data
public class ClienteLayoutEDI {

	public ClienteLayoutEDI(String tipo, Cliente cliente) {
		this.tipo = tipo;
		this.cliente = cliente;
	}

	private String tipo;
	private transient Cliente cliente;
	private String login = "";
	private String senha = "";
	private String chave = "";

}
