package br.com.utfpr.porta.seguranca.dto;

public class UsuarioDto {
		
	private String nome;
	private int[] audio;
	
	public UsuarioDto() {}
	
	public UsuarioDto(String nome, int[] audio) {
		this.nome = nome;
		this.audio = audio;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int[] getAudio() {
		return audio;
	}

	public void setAudio(int[] audio) {
		this.audio = audio;
	}
	
}
