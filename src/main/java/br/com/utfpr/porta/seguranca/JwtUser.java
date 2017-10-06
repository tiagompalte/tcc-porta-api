package br.com.utfpr.porta.seguranca;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.utfpr.porta.modelo.Porta;

public class JwtUser implements UserDetails {

	private static final long serialVersionUID = 1L;

	private Porta porta;
	private Collection<? extends GrantedAuthority> authorities;

	public JwtUser(Porta porta, Collection<? extends GrantedAuthority> authorities) {
		this.porta = porta;
		this.authorities = authorities;
	}

	@Override
	public String getUsername() {
		return porta.getCodigo().toString();
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
		return porta.getSenha();
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

