package br.com.utfpr.porta.controle.dto;

import br.com.utfpr.porta.modelo.Usuario;

public class UsuarioDto {

	private String nome;
	private String email;
	private String telefone;
	
	public UsuarioDto() {}
	
	public UsuarioDto(Usuario usuario) {
		nome = usuario.getPessoa().getNome();
		email = usuario.getEmail();
		telefone = usuario.getPessoa().getTelefone();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	
}
