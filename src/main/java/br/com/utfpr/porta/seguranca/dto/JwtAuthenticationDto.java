package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class JwtAuthenticationDto {

	@NotNull(message = "Código não informado")
	private String codigo;
	@NotNull(message = "Senha não informada")
	private String senha;

	public JwtAuthenticationDto() {}

	@NotEmpty(message = "Código não pode ser vazio.")
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@NotEmpty(message = "Senha não pode ser vazia.")
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

