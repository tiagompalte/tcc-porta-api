package br.com.utfpr.porta.controle.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class AudioDto {
	
	@NotNull(message = "RFID não informado")
	@NotEmpty(message = "RFID não pode ser vazio")
	private String rfid;
	@NotNull(message = "Áudio não informada")
	@NotEmpty(message = "Áudio não pode ser vazio")
	private String audio;
	
	public String getRfid() {
		return rfid;
	}
	public void setRfid(String rfid) {
		this.rfid = rfid;
	}
	public String getAudio() {
		return audio;
	}
	public void setAudio(String audio) {
		this.audio = audio;
	}
	
}
