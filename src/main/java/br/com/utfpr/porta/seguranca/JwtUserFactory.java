package br.com.utfpr.porta.seguranca;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.utfpr.porta.seguranca.enums.PerfilEnum;


public class JwtUserFactory {

	private JwtUserFactory() {}

	public static JwtUser create(Object object) {
		return new JwtUser(object, mapToGrantedAuthorities());
	}

	private static List<GrantedAuthority> mapToGrantedAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(PerfilEnum.ROLE_VALIDACAO.toString()));
		return authorities;
	}

}

