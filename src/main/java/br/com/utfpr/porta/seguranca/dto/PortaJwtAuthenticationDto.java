package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

public class PortaJwtAuthenticationDto implements JwtAuthenticationDto {
	
	@NotNull(message = "Código não informado") 
	private String codigo; 
	@NotNull(message = "Senha não informada") 
	private String senha;
	
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	@Override
	public String getId() {
		return getCodigo();
	}
	@Override
	public void setId(String id) {
		setCodigo(id);
	}
	@Override
	public String getSenha() {
		return senha;
	}
	@Override
	public void setSenha(String senha) {
		this.senha = senha;
	}
		
	@Override
	public String toString() {
		return "PortaJwtAuthenticationDto [codigo=" + codigo + ", senha=" + senha + "]";
	}
	
}
