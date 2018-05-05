package br.com.utfpr.porta.controle.dto;

import br.com.utfpr.porta.modelo.Log;

public class LogDto {
	
	private String acao;
	private String dataHora;
	
	public LogDto() {}
	
	public LogDto(Log log) {
		this.acao = log.getAcao();
		this.dataHora = log.getDataHoraFormatado();
	}

	public String getAcao() {
		return acao;
	}

	public void setAcao(String acao) {
		this.acao = acao;
	}

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}
}
