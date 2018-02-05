package br.com.utfpr.porta.seguranca.dto;

public class MensagemDto {
	
	private String mensagem;
	
	public MensagemDto(String mensagem) {
		super();
		this.mensagem = mensagem;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	
}
