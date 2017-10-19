package br.com.utfpr.porta.controle;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.utfpr.porta.response.Response;
import br.com.utfpr.porta.seguranca.dto.JwtAuthenticationDto;
import br.com.utfpr.porta.seguranca.dto.TokenDto;
import br.com.utfpr.porta.seguranca.util.JwtTokenUtil;
import br.com.utfpr.porta.util.Criptografia;

@Controller
@RequestMapping("/token")
@CrossOrigin(origins = "*")
public class AutenticacaoControle {
	
	private static final Logger log = LoggerFactory.getLogger(AutenticacaoControle.class);
	private static final String TOKEN_HEADER = "authorization";
	private static final String BEARER_PREFIX = "Bearer";
	private static final String CHAVE = "AzSJFHSJFBSJFHSJ";

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Gera e retorna um novo token JWT.
	 * 
	 * @param authenticationDto
	 * @param result
	 * @return ResponseEntity<Response<TokenDto>>
	 * @throws AuthenticationException
	 */
	@PostMapping
	public ResponseEntity<Response<TokenDto>> gerarTokenJwt(@Valid @RequestBody JwtAuthenticationDto authenticationDto,
			BindingResult result) throws AuthenticationException {
		
		Response<TokenDto> response = new Response<TokenDto>();

		if (result.hasErrors()) {
			log.error("Erro validando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}
		
		try {
			authenticationDto.setSenha(Criptografia.decode(authenticationDto.getSenha(), CHAVE));
		}
		catch(Exception e) {
			response.setErrors(Arrays.asList(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
				
		try {			
			log.info("Gerando token para a porta de código {}.", authenticationDto.getCodigo());
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationDto.getCodigo(), authenticationDto.getSenha()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch(Exception e) {
			response.setErrors(Arrays.asList(e.getMessage()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getCodigo());
		String token = jwtTokenUtil.obterToken(userDetails);
		response.setData(new TokenDto(token));

		return ResponseEntity.ok(response);
	}

	/**
	 * Gera um novo token com uma nova data de expiração.
	 * 
	 * @param request
	 * @return ResponseEntity<Response<TokenDto>>
	 */
	@PostMapping(value = "/refresh")
	public ResponseEntity<Response<TokenDto>> gerarRefreshTokenJwt(HttpServletRequest request) {
		log.info("Gerando refresh token JWT.");
		Response<TokenDto> response = new Response<TokenDto>();
		Optional<String> token = Optional.ofNullable(request.getHeader(TOKEN_HEADER));

		if (token.isPresent() && token.get().startsWith(BEARER_PREFIX)) {
			token = Optional.of(token.get().substring(BEARER_PREFIX.length() + 1));
		}

		if (!token.isPresent()) {
			response.getErrors().add("Token não informado.");
		} 
		
		try {
			if (!jwtTokenUtil.tokenValido(token.get())) {
				response.getErrors().add("Token inválido ou expirado.");
			}
		}
		catch(Exception e) {
			response.addError("Token inválido");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		

		if (!response.getErrors().isEmpty()) {
			return ResponseEntity.badRequest().body(response);
		}

		try {
			String refreshedToken = jwtTokenUtil.refreshToken(token.get());
			response.setData(new TokenDto(refreshedToken));
		}
		catch(Exception e) {
			response.addError(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}

		return ResponseEntity.ok(response);
	}

}
