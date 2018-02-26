package br.com.utfpr.porta.controle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
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
import br.com.utfpr.porta.seguranca.dto.UsuarioDto;
import br.com.utfpr.porta.seguranca.dto.MensagemDto;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.LogServico;
import br.com.utfpr.porta.storage.AudioStorage;

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
	private AudioStorage audioStorage;
		
	@RequestMapping(value="/rfid/{rfid}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid,
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		ErroDto erro = new ErroDto();
				
		if(Strings.isEmpty(rfid)) {
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
		
		if(Strings.isEmpty(usuario.get().getNomeAudio())) {
			erro.addError("Usuário sem áudio cadastrado");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);
		}
		
		if(autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora) == false) {
			erro.addError("Usuário sem autorização para acesso a porta desejada");
			responseErro.setData(erro);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		
		int[] audio = null;
		try {			
			if(!Strings.isEmpty(usuario.get().getNomeAudio())) {
				
				byte[] audio_byte = audioStorage.recuperar(usuario.get().getNomeAudio());
				
				if(audio_byte == null) {
					throw new NullPointerException("Áudio não pode ser recuperado");
				}
				
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audio_byte));				
				AudioFormat audioFormat = new AudioFormat(16000, 8, 1, true, audioInput.getFormat().isBigEndian());
				AudioInputStream outStream = AudioSystem.getAudioInputStream(audioFormat, audioInput);								
				File tempFile = File.createTempFile("audio", ".temp");
				AudioSystem.write(outStream, Type.WAVE, tempFile);				
				byte[] tempByte = Files.readAllBytes(tempFile.toPath());
				
				audio = new int[tempByte.length];
				for(int i = 0; i < tempByte.length; i++) {
					audio[i] = tempByte[i] + 128;
				}
																
			}
			else {
				throw new Exception("Usuário sem áudio registrado");
			}
		} catch(Exception e) {
			erro.addError("Erro ao codificar o áudio. ".concat(e.getMessage()));
			responseErro.setData(erro);		
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
				
		Response<UsuarioDto> responseMensagem = new Response<UsuarioDto>();
		String nome = (usuario.get().getPessoa() != null && Strings.isNotEmpty(usuario.get().getPessoa().getNome()) ? usuario.get().getPessoa().getNome() : "");		
		responseMensagem.setData(new UsuarioDto(nome, audio));
						
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
			erro.addError("Senha incorreta");
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
				
		logServico.entrarPorta(usuario.get(), porta, dataHora, "falada");
		
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		responseMensagem.setData(new MensagemDto("Log registrado"));
		
		return ResponseEntity.ok(responseMensagem);
	}
		
}
