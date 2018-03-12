package br.com.utfpr.porta.seguranca.dto;

import java.util.ArrayList;
import java.util.List;

public class ErroDto {
	
	private List<String> errors;
	
	public ErroDto() {
		errors = new ArrayList<>();
	}
	
	public ErroDto(String mensagemErro) {
		errors = new ArrayList<>();
		errors.add(mensagemErro);
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public void addError(String error) {
		errors.add(error);
	}

}
