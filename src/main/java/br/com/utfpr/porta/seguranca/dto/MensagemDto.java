package br.com.utfpr.porta.seguranca.dto;

public class MensagemDto {
		
	private String hash;
//	private String mensagem;
//	
//	public MensagemDto(String mensagem) {
//		super();
//		this.mensagem = mensagem;
//	}
	
//	public MensagemDto(String mensagem, String hash) {
//		super();
//		this.mensagem = mensagem;
//		this.hash = hash;
//	}
	
	public MensagemDto(String hash) {
		this.hash = hash;
	}

//	public String getMensagem() {
//		return mensagem;
//	}
//
//	public void setMensagem(String mensagem) {
//		this.mensagem = mensagem;
//	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
}
