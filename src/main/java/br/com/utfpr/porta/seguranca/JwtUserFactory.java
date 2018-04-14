package br.com.utfpr.porta.seguranca;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.seguranca.enums.PerfilEnum;


public class JwtUserFactory {

	private JwtUserFactory() {}

	/**
	 * Converte e gera um JwtUser com base nos dados de um funcionário.
	 * 
	 * @param funcionario
	 * @return JwtUser
	 */
	public static JwtUser create(Porta porta) {
		return new JwtUser(porta, mapToGrantedAuthorities());
	}

	/**
	 * Converte o perfil do usuário para o formato utilizado pelo Spring Security.
	 * 
	 * @param perfilEnum
	 * @return List<GrantedAuthority>
	 */
	private static List<GrantedAuthority> mapToGrantedAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(PerfilEnum.ROLE_VALIDACAO.toString()));
		return authorities;
	}

}

