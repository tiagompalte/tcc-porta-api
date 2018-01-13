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
import br.com.utfpr.porta.seguranca.dto.ErroDto;
import br.com.utfpr.porta.seguranca.dto.JwtAuthenticationDto;
import br.com.utfpr.porta.seguranca.dto.TokenDto;
import br.com.utfpr.porta.seguranca.util.JwtTokenUtil;

@Controller
@RequestMapping("/token")
@CrossOrigin(origins = "*")
public class AutenticacaoControle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacaoControle.class);
	private static final String TOKEN_HEADER = "authorization";
	private static final String BEARER_PREFIX = "Bearer";
	//private static final String CHAVE = "AzSJFHSJFBSJFHSJ";

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
	 * @return ResponseEntity<Response<?>>
	 * @throws AuthenticationException
	 */
	@PostMapping
	public ResponseEntity<Response<?>> gerarTokenJwt(@Valid @RequestBody JwtAuthenticationDto authenticationDto,
			BindingResult result) throws AuthenticationException {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();

		if (result.hasErrors()) {
			LOGGER.error("Erro validando lançamento: {}", result.getAllErrors());
			result.getAllErrors().forEach(error -> erro.addError(error.getDefaultMessage()));
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}
		
//		try {
//			authenticationDto.setSenha(Criptografia.decode(authenticationDto.getSenha(), CHAVE));
//		}
//		catch(Exception e) {			
//			erro.setErrors(Arrays.asList(e.getMessage()));
//			responseErro.setData(erro);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
//		}
				
		try {			
			LOGGER.info("Gerando token para a porta de código {}.", authenticationDto.getCodigo());
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationDto.getCodigo(), authenticationDto.getSenha()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch(Exception e) {
			erro.addError(e.getMessage());
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getCodigo());
		String token = jwtTokenUtil.obterToken(userDetails);
		
		Response<TokenDto> responseToken = new Response<TokenDto>();
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
	public ResponseEntity<Response<?>> gerarRefreshTokenJwt(HttpServletRequest request) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();
		Optional<String> token = Optional.ofNullable(request.getHeader(TOKEN_HEADER));

		if (token.isPresent() && token.get().startsWith(BEARER_PREFIX)) {
			token = Optional.of(token.get().substring(BEARER_PREFIX.length() + 1));
		}

		if (!token.isPresent()) {			
			erro.addError("Token não informado.");
		} 
		
		try {
			if (!jwtTokenUtil.tokenValido(token.get())) {
				erro.addError("Token inválido ou expirado.");
			}
		}
		catch(Exception e) {
			erro.addError("Token inválido");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}

		if (!erro.getErrors().isEmpty()) {
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}

		Response<TokenDto> responseToken = new Response<TokenDto>();
		try {
			String refreshedToken = jwtTokenUtil.refreshToken(token.get());
			responseToken.setData(new TokenDto(refreshedToken));
		}
		catch(Exception e) {
			erro.addError(e.getMessage());
			responseErro.setData(erro);
			return ResponseEntity.badRequest().body(responseErro);
		}

		return ResponseEntity.ok(responseToken);
	}

}
