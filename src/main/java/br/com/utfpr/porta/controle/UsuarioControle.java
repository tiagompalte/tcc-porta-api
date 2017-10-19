package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
	public ResponseEntity<?> obterUsuarioPorRFID(@RequestHeader(value="zone") String zone,
			@RequestHeader(value="porta") Long codigo_porta, @PathVariable String rfid) {
		
		Response<TokenDto> response = new Response<TokenDto>();
		
		if(StringUtils.isEmpty(rfid)) {
			response.addError("RFID não informado");
		}
		
		if(codigo_porta == null) {
			response.addError("Código da porta não informado");
		}
		
		if(Strings.isEmpty(zone)) {
			response.addError("Zona local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			response.addError("Zona local formatada incorretamente");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			response.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			response.addError("Usuário sem autorização para acesso a porta desejada");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
				
		return ResponseEntity.status(HttpStatus.OK).body(usuario);			
	}
	
	@RequestMapping(value="/autenticacaoSenha", method=RequestMethod.POST)
	public ResponseEntity<?> autenticacaoPorSenhaDigitada(@RequestHeader(value="zone") String zone,
			@RequestHeader(value="porta") Long codigo_porta,
			@Valid @RequestBody AutenticacaoSenhaDto autenticacaoSenha) {
		
		Response<TokenDto> response = new Response<TokenDto>();
				
		if(codigo_porta == null) {
			response.addError("Código da porta não informado");
		}
		
		if(Strings.isEmpty(zone)) {
			response.addError("Zona local não informada");
		}
		
		if(response.getErrors() != null && !response.getErrors().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);			
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			response.addError("Zona local formatada incorretamente");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		
		Usuario usuario = usuariosRepositorio.findByRfid(autenticacaoSenha.getRfid());
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			response.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			response.addError("Usuário sem autorização para acesso a porta desejada");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
		
		if(!BCrypt.checkpw(autenticacaoSenha.getSenha(), usuario.getSenha())) {
			response.addError("Senha incorreta");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
						
		logServico.entrarPorta(usuario, porta);
		
		response.setData(new TokenDto("Autorizado"));
				
		return ResponseEntity.ok(response);			
	}
	
	@RequestMapping(value="/confirmacaoAcesso/{rfid}", method=RequestMethod.POST)
	public ResponseEntity<?> confirmacaoAcesso(@RequestHeader(value="porta") Long codigo_porta, @PathVariable String rfid) {
		
		Response<TokenDto> response = new Response<TokenDto>();
		
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			response.addError("Usuário e/ou porta não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		logServico.entrarPorta(usuario, porta);
		
		response.setData(new TokenDto("Log registrado"));
		
		return ResponseEntity.ok(response);
	}
	

}
