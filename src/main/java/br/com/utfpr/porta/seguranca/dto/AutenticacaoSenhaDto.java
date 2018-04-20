package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class AutenticacaoSenhaDto {
	
	@NotNull(message = "RFID não informado")
	@NotEmpty(message = "RFID não pode ser vazio")
	private String rfid;
	@NotNull(message = "Senha não informada")
	@NotEmpty(message = "Senha não pode ser vazia")
	private String senha;
	
	public String getRfid() {
		return rfid;
	}

	public void setRfid(String rfid) {
		this.rfid = rfid;
	}
	
	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	@Override
	public String toString() {
		return "AutenticacaoSenhaDto [rfid=" + rfid + ", senha=" + senha + "]";
	}


}
