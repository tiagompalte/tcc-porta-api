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

import br.com.utfpr.porta.controle.exception.BadRequestException;
import br.com.utfpr.porta.controle.exception.NotAcceptableException;
import br.com.utfpr.porta.controle.exception.NotFoundException;
import br.com.utfpr.porta.controle.exception.UnauthorizedException;
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
		
	private LocalDateTime converterZoneParaLocalDateTime(String zone) throws BadRequestException {	
		
		if(Strings.isEmpty(zone)) {
			throw new BadRequestException("Zona local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.now(ZoneId.of(zone));
		}
		catch(Exception e) {
			throw new BadRequestException("Zona local formatada incorretamente");
		}		
		return dataHora;
	}
	
	private Optional<Usuario> obterUsuario(String rfid) {
		
		if(Strings.isEmpty(rfid)) {
			throw new BadRequestException("RFID não informado");
		}
		
		Optional<Usuario> usuario = usuariosRepositorio.findByRfid(rfid);
		
		if(!usuario.isPresent()) {
			throw new NotFoundException("Usuário não encontrado");
		}
		
		if(usuario.get().getAtivo() == false) {
			throw new NotAcceptableException("Usuário inativo");
		}
		
		if(Strings.isEmpty(usuario.get().getNomeAudio())) {
			throw new NotFoundException("Usuário sem áudio cadastrado");
		}
		
		return usuario;		
	}
	
	private Porta obterPorta(Long codigo_porta) {
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(porta == null) {
			throw new NotFoundException("Porta não encontrada");
		}
		
		return porta;
	}
	
	private int[] converterAudioEmArrayInt(String nome_audio) throws Exception {
		
		if(Strings.isEmpty(nome_audio)) {
			throw new Exception("Usuário sem áudio registrado");
		}
		
		int[] audio = null;
		try {			
			
			byte[] audio_byte = audioStorage.recuperar(nome_audio);
			
			if(audio_byte == null) {
				throw new NullPointerException("Áudio não pode ser recuperado");
			}
			
			AudioInputStream audioInput = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audio_byte));				
			AudioFormat audioFormat = new AudioFormat(16000, 8, 1, true, audioInput.getFormat().isBigEndian());				
			AudioInputStream outStream = AudioSystem.getAudioInputStream(audioFormat, audioInput);								
			File tempFile = File.createTempFile("audio", ".temp");
			AudioSystem.write(outStream, Type.WAVE, tempFile);				
			byte[] tempByte = Files.readAllBytes(tempFile.toPath());
			
			//Inicia do byte 44 para pular o metadata do arquivo wav				
			audio = new int[tempByte.length - 44];
			for(int i = 0; i < tempByte.length - 44; i++) {
				audio[i] = tempByte[i + 44] + 128;
			}
		} catch(Exception e) {
			throw new Exception("Erro ao codificar o áudio. ".concat(e.getMessage()));
		}
		
		return audio;
	}
	
	@RequestMapping(value="/rfid/{rfid}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid,
			HttpServletRequest request, HttpServletResponse response) {
		
		return obterUsuarioPorRFIDversao1(zone, rfid, request, response);
	}
		
	@RequestMapping(value="/rfid/{rfid}", method=RequestMethod.GET, headers = "X-API-Version=v1")
	public ResponseEntity<?> obterUsuarioPorRFIDversao1(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid,
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		Response<UsuarioDto> responseMensagem = new Response<UsuarioDto>();
		
		try {
			
			if(request.getAttribute("codigo_porta") == null) {
				throw new BadRequestException("Código da porta não informado");
			}
			
			Long codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
			
			LocalDateTime dataHora = converterZoneParaLocalDateTime(zone);
			
			Optional<Usuario> usuario = obterUsuario(rfid);
			
			Porta porta = obterPorta(codigo_porta);
			
			if(autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora) == false) {
				throw new UnauthorizedException("Usuário sem autorização para acesso a porta desejada");
			}
			
			int[] audio = converterAudioEmArrayInt(usuario.get().getNomeAudio());
			
			
			String nome = (usuario.get().getPessoa() != null && Strings.isNotEmpty(usuario.get().getPessoa().getNome()) 
									? usuario.get().getPessoa().getNome() : "");		
			responseMensagem.setData(new UsuarioDto(nome, audio));
			
		}
		catch(BadRequestException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);			
		}
		catch(NotFoundException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);	
		}
		catch(NotAcceptableException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		catch(UnauthorizedException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		catch(Exception e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
						
		return ResponseEntity.ok(responseMensagem);		
	}
	
	@RequestMapping(value="/autenticacaoSenha", method=RequestMethod.POST)
	public ResponseEntity<?> autenticacaoPorSenhaDigitada(@RequestHeader(value="zone") String zone,
			HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody AutenticacaoSenhaDto autenticacaoSenha) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		
		try {
			
			if(request.getAttribute("codigo_porta") == null) {
				throw new BadRequestException("Código da porta não informado");
			}
			
			Long codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
			
			LocalDateTime dataHora = converterZoneParaLocalDateTime(zone);
			
			Optional<Usuario> usuario = obterUsuario(autenticacaoSenha.getRfid());
			
			Porta porta = obterPorta(codigo_porta);
			
			if(autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora) == false) {
				throw new UnauthorizedException("Usuário sem autorização para acesso a porta desejada");
			}
			
			PasswordEncoder pass = new BCryptPasswordEncoder();
			
			if(pass.matches(autenticacaoSenha.getSenha(), usuario.get().getSenhaTeclado()) == false) {
				throw new UnauthorizedException("Senha incorreta");
			}
							
			logServico.entrarPorta(usuario.get(), porta, dataHora, "digitada");
						
			responseMensagem.setData(new MensagemDto("Autorizado"));
			
		}
		catch(BadRequestException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);			
		}
		catch(NotFoundException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);	
		}
		catch(NotAcceptableException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		catch(UnauthorizedException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		catch(Exception e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
		
		return ResponseEntity.ok(responseMensagem);			
	}
	
	@RequestMapping(value="/confirmacaoAcesso/{rfid}", method=RequestMethod.POST)
	public ResponseEntity<?> confirmacaoAcesso(
			@RequestHeader(value="zone") String zone, @PathVariable String rfid, 
			HttpServletRequest request, HttpServletResponse response) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		Response<MensagemDto> responseMensagem = new Response<MensagemDto>();
		
		try {
			
			if(request.getAttribute("codigo_porta") == null) {
				throw new BadRequestException("Código da porta não informado");
			}
			
			Long codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
			
			LocalDateTime dataHora = converterZoneParaLocalDateTime(zone);
			
			Optional<Usuario> usuario = obterUsuario(rfid);
			
			Porta porta = obterPorta(codigo_porta);
			
			logServico.entrarPorta(usuario.get(), porta, dataHora, "falada");
						
			responseMensagem.setData(new MensagemDto("Log registrado"));
			
		}
		catch(BadRequestException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);			
		}
		catch(NotFoundException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);	
		}
		catch(NotAcceptableException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseErro);
		}
		catch(UnauthorizedException e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseErro);
		}
		catch(Exception e) {
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
		
		return ResponseEntity.ok(responseMensagem);	
	}
		
}
