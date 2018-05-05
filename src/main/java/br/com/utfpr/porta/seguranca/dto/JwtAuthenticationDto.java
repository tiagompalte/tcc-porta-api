package br.com.utfpr.porta.seguranca.dto;

import br.com.utfpr.porta.seguranca.enums.TypeJwt;

public interface JwtAuthenticationDto {
	
	public String getId();
	
	public void setId(String id);
	
	public String getSenha();
	
	public void setSenha(String senha);
	
	public TypeJwt getType();	

}

