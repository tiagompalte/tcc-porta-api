package br.com.utfpr.porta.seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.repositorio.Portas;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private Portas portasRepositorio;

	@Override
	public UserDetails loadUserByUsername(String username) {
					
		Porta porta = portasRepositorio.findOne(Long.valueOf(username));

		if (porta != null) {
			return JwtUserFactory.create(porta);
		}

		throw new UsernameNotFoundException("Porta não encontrada.");
	}

}
