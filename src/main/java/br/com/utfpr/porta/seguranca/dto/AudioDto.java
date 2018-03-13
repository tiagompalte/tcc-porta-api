package br.com.utfpr.porta.seguranca.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class AudioDto {
	
	@NotNull(message = "RFID não informado")
	@NotEmpty(message = "RFID não pode ser vazio")
	private String rfid;
	@NotNull(message = "Áudio não informada")
	@NotEmpty(message = "Áudio não pode ser vazio")
	private int[] audio;
	
	public String getRfid() {
		return rfid;
	}
	public void setRfid(String rfid) {
		this.rfid = rfid;
	}
	public int[] getAudio() {
		return audio;
	}
	public void setAudio(int[] audio) {
		this.audio = audio;
	}
	
}
