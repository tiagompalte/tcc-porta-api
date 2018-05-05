package br.com.utfpr.porta.seguranca;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {
		
	private static final String NUMERO_REGEX = "^\\d+$";

	@Autowired
	private Portas portasRepositorio;
	
	@Autowired
	private Usuarios usuariosRepositorio;

	@Override
	public UserDetails loadUserByUsername(String username) {
		
		if(Strings.isEmpty(username)) {
			throw new UsernameNotFoundException("Username não informado");
		}
		
		if(username.matches(NUMERO_REGEX)) {
			
			Porta porta = portasRepositorio.findOne(Long.valueOf(username));
			
			if (porta != null) {
				return JwtUserFactory.create(porta);
			}
			
			throw new UsernameNotFoundException("Porta não encontrada");
		}
		else {
			Optional<Usuario> usuario = usuariosRepositorio.porEmailEAtivo(username);
			
			if(usuario.isPresent()) {
				return JwtUserFactory.create(usuario.get());
			}		

			throw new UsernameNotFoundException("Usuário não encontrado");
		}
					
	}
	
}
