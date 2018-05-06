package br.com.utfpr.porta.seguranca;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.seguranca.dto.exception.EntidadeNaoDefinidaException;

public class JwtUser implements UserDetails {

	private static final long serialVersionUID = 1L;

	private Porta porta;
	private Usuario usuario;
	
	private Collection<? extends GrantedAuthority> authorities;

	public JwtUser(Object object, Collection<? extends GrantedAuthority> authorities) {
		
		this.authorities = authorities;
				
		try {
			this.usuario = Usuario.class.cast(object);
		}
		catch(ClassCastException e) {
			this.porta = Porta.class.cast(object);
		}	
		
		if(usuario == null && porta == null) {
			throw new EntidadeNaoDefinidaException();
		}
	}

	@Override
	public String getUsername() {
		if(porta != null) {
			return porta.getCodigo().toString();			
		}
		else if(usuario != null) {
			return usuario.getEmail();
		}
		return null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public String getPassword() {
		if(porta != null) {
			return porta.getSenha();			
		}
		else if(usuario != null) {
			return usuario.getSenhaSite();
		}
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}

