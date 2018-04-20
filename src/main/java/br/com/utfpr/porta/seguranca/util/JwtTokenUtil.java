package br.com.utfpr.porta.seguranca.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

	private static final String CLAIM_KEY_USERNAME = "sub";
	private static final String CLAIM_KEY_ROLE = "role";
	private static final String CLAIM_KEY_CREATED = "created";

	private static final String SECRET = "4_@Q&9tZ&Ly?Fjn0>>Z6(077`KGluC";
	private static final Long EXPIRATION = 86400L; //Um dia = 60 x 60 x 24

	/**
	 * Obtém o username (email) contido no token JWT.
	 * 
	 * @param token
	 * @return String
	 */
	public String getUsernameFromToken(String token) {
		return getClaimsFromToken(token).getSubject();
	}

	/**
	 * Retorna a data de expiração de um token JWT.
	 * 
	 * @param token
	 * @return Date
	 */
	public Date getExpirationDateFromToken(String token) {
		return getClaimsFromToken(token).getExpiration();
	}

	/**
	 * Cria um novo token (refresh).
	 * 
	 * @param token
	 * @return String
	 */
	public String refreshToken(String token) {
		Claims claims = getClaimsFromToken(token);
		claims.put(CLAIM_KEY_CREATED, new Date());
		return gerarToken(claims);
	}

	/**
	 * Verifica e retorna se um token JWT é válido.
	 * 
	 * @param token
	 * @return boolean
	 */
	public boolean tokenValido(String token) {
		return !tokenExpirado(token);
	}

	/**
	 * Retorna um novo token JWT com base nos dados do usuários.
	 * 
	 * @param userDetails
	 * @return String
	 */
	public String obterToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		userDetails.getAuthorities().forEach(authority -> claims.put(CLAIM_KEY_ROLE, authority.getAuthority()));
		claims.put(CLAIM_KEY_CREATED, new Date());
		return gerarToken(claims);
	}

	/**
	 * Realiza o parse do token JWT para extrair as informações contidas no corpo
	 * dele.
	 * 
	 * @param token
	 * @return Claims
	 */
	private Claims getClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
	}

	/**
	 * Retorna a data de expiração com base na data atual.
	 * 
	 * @return Date
	 */
	private Date gerarDataExpiracao() {
		return new Date(System.currentTimeMillis() + EXPIRATION * 1000);
	}

	/**
	 * Verifica se um token JTW está expirado.
	 * 
	 * @param token
	 * @return boolean
	 */
	private boolean tokenExpirado(String token) {
		Date dataExpiracao = this.getExpirationDateFromToken(token);
		return dataExpiracao.before(new Date());
	}

	/**
	 * Gera um novo token JWT contendo os dados (claims) fornecidos.
	 * 
	 * @param claims
	 * @return String
	 */
	private String gerarToken(Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(gerarDataExpiracao())
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
	}	

}

