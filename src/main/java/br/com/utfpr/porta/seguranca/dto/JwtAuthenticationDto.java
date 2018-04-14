package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class JwtAuthenticationDto {

	@NotNull(message = "Código não informado")
	@NotEmpty(message = "Código não pode ser vazio")
	private String codigo;
	@NotNull(message = "Senha não informada")
	@NotEmpty(message = "Senha não pode ser vazia")
	private String senha;
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	@Override
	public String toString() {
		return "JwtAuthenticationRequestDto [codigo=" + codigo + ", senha=" + senha + "]";
	}

}

