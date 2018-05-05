package br.com.utfpr.porta.controle.dto;

public class UsuarioAcessoDto {
	
	private String mensagem;
	private String nome;
	
	public UsuarioAcessoDto(String mensagem, String nome) {
		super();
		this.mensagem = mensagem;
		this.nome = nome;
	}
	
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}	
}
