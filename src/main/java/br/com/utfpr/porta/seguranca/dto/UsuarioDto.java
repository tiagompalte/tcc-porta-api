package br.com.utfpr.porta.seguranca.dto;

public class UsuarioDto {
		
	private String hash;
	private String nome;
	//private String audio;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

//	public String getAudio() {
//		return audio;
//	}
//
//	public void setAudio(String audio) {
//		this.audio = audio;
//	}
}
