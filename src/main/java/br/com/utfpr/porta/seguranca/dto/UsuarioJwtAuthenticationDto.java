package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

public class UsuarioJwtAuthenticationDto implements JwtAuthenticationDto{

	@NotNull(message = "E-mail não informado")
	private String email;
	@NotNull(message = "Senha não informada")
	private String senha;
		
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getId() {
		return getEmail();
	}
	
	@Override
	public void setId(String email) {
		setEmail(email);
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
		return "UsuarioJwtAuthenticationDto [email=" + email + ", senha=" + senha + "]";
	}
	
}
