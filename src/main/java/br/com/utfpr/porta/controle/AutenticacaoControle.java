package br.com.utfpr.porta.controle;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.utfpr.porta.controle.dto.ErroDto;
import br.com.utfpr.porta.controle.dto.TokenDto;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.response.Response;
import br.com.utfpr.porta.seguranca.dto.JwtAuthenticationDto;
import br.com.utfpr.porta.seguranca.dto.PortaJwtAuthenticationDto;
import br.com.utfpr.porta.seguranca.dto.UsuarioJwtAuthenticationDto;
import br.com.utfpr.porta.seguranca.util.JwtTokenUtil;

@Controller
@RequestMapping("/token")
@CrossOrigin(origins = "*")
public class AutenticacaoControle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacaoControle.class);
	private static final String TOKEN_HEADER = "authorization";
	private static final String BEARER_PREFIX = "Bearer";

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private Portas portaRepositorio;
	
	@Autowired
	private Usuarios usuarioRepositorio;
	
	private String gerarTokenJwt(JwtAuthenticationDto authenticationDto) {
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authenticationDto.getId(), authenticationDto.getSenha()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getId());
		return jwtTokenUtil.obterToken(userDetails);		
	}
	
	/**
	 * Gerar token para acesso das portas
	 * @param portaJwtDto
	 * @param result
	 * @return
	 */
	@PostMapping("/porta")
	public ResponseEntity<Response> gerarTokenJwtPorta(@Valid @RequestBody PortaJwtAuthenticationDto portaJwtDto, BindingResult result) {
		
		Response<ErroDto> responseErro = new Response<>();
		ErroDto erro = new ErroDto();

		if (result.hasErrors()) {
			LOGGER.error("Erro validando JWT Porta: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> erro.addError(error.getDefaultMessage()));
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}
					
		String token = "";
		try {	
			
			if(!portaRepositorio.exists(Long.parseLong(portaJwtDto.getId()))) {
				throw new BadCredentialsException("Porta não existe");
			}
			
			token = gerarTokenJwt(portaJwtDto);
		}
		catch(BadCredentialsException e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto("Código e/ou senha não conferem"));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		catch(NumberFormatException e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto("Código da porta não identificado"));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
		
		Response<TokenDto> responseToken = new Response<>();
		responseToken.setData(new TokenDto(token));
		return ResponseEntity.ok(responseToken);
	}
	
	/**
	 * Gerar token para acesso dos usuários via aplicativo
	 * @param usuarioJwtDto
	 * @param result
	 * @return
	 */
	@PostMapping("/usuario")
	public ResponseEntity<Response> gerarTokenJwtUsuario(@Valid @RequestBody UsuarioJwtAuthenticationDto usuarioJwtDto, BindingResult result) {
		
		Response<ErroDto> responseErro = new Response<>();
		ErroDto erro = new ErroDto();

		if (result.hasErrors()) {
			LOGGER.error("Erro validando JWT Usuário: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> erro.addError(error.getDefaultMessage()));
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}
			
		String token = "";
		try {		
			
			if(!usuarioRepositorio.findByEmail(usuarioJwtDto.getEmail()).isPresent()) {
				throw new BadCredentialsException("Usuário não existe");
			}
			
			token = gerarTokenJwt(usuarioJwtDto);
		}
		catch(BadCredentialsException e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto("E-mail e/ou senha não conferem"));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
		
		Response<TokenDto> responseToken = new Response<>();
		responseToken.setData(new TokenDto(token));
		return ResponseEntity.ok(responseToken);
	}
	
	/**
	 * Gera um novo token com uma nova data de expiração.
	 * 
	 * @param request
	 * @return ResponseEntity<Response<?>>
	 */
	@PostMapping(value = "/refresh")
	public ResponseEntity<Response> gerarRefreshTokenJwt(HttpServletRequest request) {
		
		Response<ErroDto> responseErro = new Response<>();
		ErroDto erro = new ErroDto();
		Optional<String> token = Optional.ofNullable(request.getHeader(TOKEN_HEADER));

		if (token.isPresent() && token.get().startsWith(BEARER_PREFIX)) {
			token = Optional.of(token.get().substring(BEARER_PREFIX.length() + 1));
		}

		if (!token.isPresent()) {			
			erro.addError("Token não informado.");
		} 
		
		try {
			if (token.isPresent() && !jwtTokenUtil.tokenValido(token.get())) {
				erro.addError("Token inválido ou expirado.");
			}
		}
		catch(Exception e) {
			erro.addError("Token inválido");
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}

		if (!erro.getErrors().isEmpty()) {
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}

		Response<TokenDto> responseToken = new Response<>();
		try {
			String refreshedToken = jwtTokenUtil.refreshToken(token.isPresent() ? token.get() : null);
			responseToken.setData(new TokenDto(refreshedToken));
		}
		catch(Exception e) {
			LOGGER.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.badRequest().body(responseErro);
		}

		return ResponseEntity.ok(responseToken);
	}

}
