package br.com.utfpr.porta.seguranca.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.utfpr.porta.seguranca.util.JwtTokenUtil;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	private static final String AUTH_HEADER = "authorization";
	private static final String BEARER_PREFIX = "Bearer";

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		String token = request.getHeader(AUTH_HEADER);
		
		if (token != null && token.startsWith(BEARER_PREFIX) && token.length() > BEARER_PREFIX.length() + 1) {
			token = token.substring(BEARER_PREFIX.length() + 1);
		}
		
		if(token == null || token.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("\"cod\": 400");
			response.getWriter().flush();
			response.getWriter().close();
			return;
		}
		
		String username = null;
		try {
			username = jwtTokenUtil.getUsernameFromToken(token);
		}
		catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("\"cod\": 401");
			response.getWriter().flush();
			response.getWriter().close();
			return;
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

			try {
				if (jwtTokenUtil.tokenValido(token)) {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
			catch(Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("\"cod\": 401");
				response.getWriter().flush();
				response.getWriter().close();
				return;
			}
			
		}
		
		if(username != null) {
			request.setAttribute("codigo_porta", username);
		}		

		chain.doFilter(request, response);
	}

}

