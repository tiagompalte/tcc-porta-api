package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import br.com.utfpr.porta.seguranca.dto.ErroDto;
import br.com.utfpr.porta.seguranca.dto.MensagemDto;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.LogServico;
import br.com.utfpr.porta.servico.UsuarioServico;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.util.Conversao;
import br.com.utfpr.porta.util.Hash;

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
	
	@Autowired
	private UsuarioServico usuarioServico;
	
	@Autowired
	private AudioStorage audioStorage;
		
	@RequestMapping(value="/rfid/{rfid}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid,
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();
				
		if(StringUtils.isEmpty(rfid)) {
			erro.addError("RFID não informado");
		}
		
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			erro.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
		
		if(Strings.isEmpty(zone)) {
			erro.addError("Zona local não informada");
		}
		
		if(erro.getErrors() != null && erro.getErrors().isEmpty() == false) {
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			erro.addError("Zona local formatada incorretamente");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}
		
		Optional<Usuario> usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(!usuario.isPresent() || porta == null) {
			erro.addError("Usuário e/ou porta não encontrado");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);
		}
		
		if(usuario.get().getAtivo() == false) {
			erro.addError("Usuário inativo");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		
		if(StringUtils.isEmpty(usuario.get().getNomeAudio())) {
			erro.addError("Usuário sem áudio cadastrado");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora)) {
			erro.addError("Usuário sem autorização para acesso a porta desejada");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		
		try {			
			if(!StringUtils.isEmpty(usuario.get().getNomeAudio())) {
				
				byte[] sound = audioStorage.recuperar(usuario.get().getNomeAudio());
																
				byte[] alaw = Conversao.convertWavToAlaw(sound);
				
				usuario.get().setAudio(Base64.encodeBase64String(alaw));
												
			}
		} catch(Exception e) {
			erro.addError("Erro ao codificar o áudio. ".concat(e.getMessage()));
			responseErro.setData(erro);		
		}
		
		String hash;
		try {
			hash = Hash.MD5(usuario.get().getAudio());
		} catch(Exception e) {
			erro.addError("Erro ao gerar hash da mensagem. ".concat(e.getMessage()));
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
				
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		responseMensagem.setData(new MensagemDto(usuario.get().getAudio(), hash));
						
		return ResponseEntity.ok().body(responseMensagem);			
	}
	
	@RequestMapping(value="/autenticacaoSenha", method=RequestMethod.POST)
	public ResponseEntity<?> autenticacaoPorSenhaDigitada(@RequestHeader(value="zone") String zone,
			HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody AutenticacaoSenhaDto autenticacaoSenha) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();
						
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			erro.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
		
		if(Strings.isEmpty(zone)) {
			erro.addError("Zona local não informada");
		}
		
		if(erro.getErrors() != null && !erro.getErrors().isEmpty()) {
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);			
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			erro.addError("Zona local formatada incorretamente");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}
		
		Optional<Usuario> usuario = usuariosRepositorio.findByRfid(autenticacaoSenha.getRfid());
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
			
		if(!usuario.isPresent() || porta == null) {
			erro.addError("Usuário e/ou porta não encontrado");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);
		}
		if(usuario.get().getAtivo() == false) {
			erro.addError("Usuário inativo");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora)) {
			erro.addError("Usuário sem autorização para acesso a porta desejada");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		
		PasswordEncoder pass = new BCryptPasswordEncoder();
		
		if(pass.matches(autenticacaoSenha.getSenha(), usuario.get().getSenhaTeclado()) == false) {
			String retorno = usuarioServico.incrementarNrTentativaAcessoPorta(usuario.get().getCodigo());
			erro.addError("Senha incorreta".concat(retorno != null ? retorno : ""));
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
						
		logServico.entrarPorta(usuario.get(), porta, dataHora, "digitada");
		
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		responseMensagem.setData(new MensagemDto("Autorizado"));
				
		return ResponseEntity.ok(responseMensagem);			
	}
	
	@RequestMapping(value="/confirmacaoAcesso/{rfid}", method=RequestMethod.POST)
	public ResponseEntity<?> confirmacaoAcesso(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid, 
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();
		
		if(Strings.isEmpty(zone)) {
			erro.addError("Zona local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			erro.addError("Zona local formatada incorretamente");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);
		}
						
		Long codigo_porta = null;
		if(request.getAttribute("codigo_porta") == null) {
			erro.addError("Código da porta não informado");
		}
		else {
			codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
		}
						
		Optional<Usuario> usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(!usuario.isPresent() || porta == null) {
			erro.addError("Usuário e/ou porta não encontrado");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);
		}
		
		if(usuario.get().getAtivo() == false) {
			erro.addError("Usuário inativo");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		
		if(usuario.get().getNrTentativaAcessoPorta() != null && usuario.get().getNrTentativaAcessoPorta() > 0) {
			usuario.get().setNrTentativaAcessoPorta(0);
			usuarioServico.salvar(usuario.get());
		}
		
		logServico.entrarPorta(usuario.get(), porta, dataHora, "falada");
		
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		responseMensagem.setData(new MensagemDto("Log registrado"));
		
		return ResponseEntity.ok(responseMensagem);
	}
		
}
