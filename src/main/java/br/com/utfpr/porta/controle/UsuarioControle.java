package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.response.Response;
import br.com.utfpr.porta.seguranca.dto.AutenticacaoSenhaDto;
import br.com.utfpr.porta.seguranca.dto.TokenDto;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.LogServico;

@Controller
@RequestMapping("/api/usuarios")
public class UsuarioControle {
		
	@Autowired
	private Portas portasRepositorio;
	
	@Autowired
	private LogServico logServico;
	
	@Autowired
	private AutorizacaoServico autorizacaoServico;
	
	@Autowired
	private Usuarios usuariosRepositorio;
		
	@RequestMapping(value="/rfid/{rfid}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid,
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<TokenDto> responseBody = new Response<TokenDto>();
				
		if(StringUtils.isEmpty(rfid)) {
			responseBody.addError("RFID não informado");
		}
		
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			responseBody.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
		
		if(Strings.isEmpty(zone)) {
			responseBody.addError("Zona local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			responseBody.addError("Zona local formatada incorretamente");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
		}
		
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			responseBody.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			responseBody.addError("Usuário sem autorização para acesso a porta desejada");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseBody);
		}
				
		return ResponseEntity.status(HttpStatus.OK).body(usuario);			
	}
	
	@RequestMapping(value="/autenticacaoSenha", method=RequestMethod.POST)
	public ResponseEntity<?> autenticacaoPorSenhaDigitada(@RequestHeader(value="zone") String zone,
			HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody AutenticacaoSenhaDto autenticacaoSenha) {
		
		Response<TokenDto> responseBody = new Response<TokenDto>();
						
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			responseBody.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
		
		if(Strings.isEmpty(zone)) {
			responseBody.addError("Zona local não informada");
		}
		
		if(responseBody.getErrors() != null && !responseBody.getErrors().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);			
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			responseBody.addError("Zona local formatada incorretamente");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
		}
		
		Usuario usuario = usuariosRepositorio.findByRfid(autenticacaoSenha.getRfid());
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			responseBody.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			responseBody.addError("Usuário sem autorização para acesso a porta desejada");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseBody);
		}
		
		if(!BCrypt.checkpw(autenticacaoSenha.getSenha(), usuario.getSenha())) {
			responseBody.addError("Senha incorreta");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
		}
						
		logServico.entrarPorta(usuario, porta, dataHora, "digitada");
		
		responseBody.setData(new TokenDto("Autorizado"));
				
		return ResponseEntity.ok(responseBody);			
	}
	
	@RequestMapping(value="/confirmacaoAcesso/{rfid}", method=RequestMethod.POST)
	public ResponseEntity<?> confirmacaoAcesso(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid, 
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<TokenDto> responseBody = new Response<TokenDto>();
		
		if(Strings.isEmpty(zone)) {
			responseBody.addError("Zona local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			responseBody.addError("Zona local formatada incorretamente");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
		}
						
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			responseBody.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
						
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			responseBody.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
		}
		
		logServico.entrarPorta(usuario, porta, dataHora, "falada");
		
		responseBody.setData(new TokenDto("Log registrado"));
		
		return ResponseEntity.ok(responseBody);
	}
	

}
