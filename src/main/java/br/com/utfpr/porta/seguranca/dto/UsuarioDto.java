package br.com.utfpr.porta.seguranca.dto;

public class UsuarioDto {
		
	private String nome;
	private String[] audio;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String[] getAudio() {
		return audio;
	}

	public void setAudio(String[] audio) {
		this.audio = audio;
	}
	
}
