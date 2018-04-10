package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.utfpr.porta.controle.exception.BadRequestException;
import br.com.utfpr.porta.controle.exception.NotAcceptableException;
import br.com.utfpr.porta.controle.exception.NotFoundException;
import br.com.utfpr.porta.controle.exception.UnauthorizedException;
import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Parametros;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.response.Response;
import br.com.utfpr.porta.seguranca.dto.AudioDto;
import br.com.utfpr.porta.seguranca.dto.AutenticacaoSenhaDto;
import br.com.utfpr.porta.seguranca.dto.ErroDto;
import br.com.utfpr.porta.seguranca.dto.UsuarioAcessoDto;
import br.com.utfpr.porta.servico.AutorizacaoServico;
import br.com.utfpr.porta.servico.LogServico;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.util.Algorithm;
import br.com.utfpr.porta.util.Conversao;


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
	
	@Autowired
	private Parametros parametrosRepositorio;
		
	private LocalDateTime converterZoneParaLocalDateTime(String zone) {	
		
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
	
	@RequestMapping(value="/autenticacaoSenha", method=RequestMethod.POST)
	public ResponseEntity<?> autenticacaoPorSenhaDigitada(@RequestHeader(value="zone") String zone,
			HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody AutenticacaoSenhaDto autenticacaoSenha) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		Response<UsuarioAcessoDto> responseMensagem = new Response<UsuarioAcessoDto>();
		
		try {
			
			if(request.getAttribute("codigo_porta") == null) {
				throw new BadRequestException("Código da porta não informado");
			}
			
			Long codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
			
			LocalDateTime dataHora = converterZoneParaLocalDateTime(zone);
			
			Optional<Usuario> usuario = obterUsuario(Conversao.convertHexToDecimal(autenticacaoSenha.getRfid(), true));
			
			Porta porta = obterPorta(codigo_porta);
			
			if(autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora) == false) {
				throw new UnauthorizedException("Usuário sem autorização para acesso a porta desejada");
			}
			
			PasswordEncoder pass = new BCryptPasswordEncoder();
			
			if(pass.matches(autenticacaoSenha.getSenha(), usuario.get().getSenhaTeclado()) == false) {
				throw new UnauthorizedException("Senha incorreta");
			}
							
			logServico.entrarPorta(usuario.get(), porta, dataHora, "digitada");
						
			responseMensagem.setData(new UsuarioAcessoDto("Autorizado", usuario.get().getPessoa().getNome()));
			
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
			
	@RequestMapping(value="/audio", method=RequestMethod.POST)
	public ResponseEntity<?> validarAudio(@RequestHeader(value="zone") String zone,
			HttpServletRequest request, HttpServletResponse response,
			@Valid @RequestBody AudioDto audioDto) {
		
		Response<ErroDto> responseErro = new Response<ErroDto>();
		Response<UsuarioAcessoDto> responseMensagem = new Response<UsuarioAcessoDto>();
		
		try {
			
			if(request.getAttribute("codigo_porta") == null) {
				throw new BadRequestException("Código da porta não informado");
			}
			
			Long codigo_porta = Long.parseLong(request.getAttribute("codigo_porta").toString());
			
			LocalDateTime dataHora = converterZoneParaLocalDateTime(zone);
			
			Optional<Usuario> usuario = obterUsuario(Conversao.convertHexToDecimal(audioDto.getRfid(), true));
			
			Porta porta = obterPorta(codigo_porta);
			
			if(autorizacaoServico.validarAcessoUsuario(porta, usuario.get(), dataHora) == false) {
				throw new UnauthorizedException("Usuário sem autorização para acesso a porta desejada");
			}
			
			Parametro par_tolerancia = parametrosRepositorio.findOne("TOLERANCIA");
			double tolerancia = 0.0;
			
			if(par_tolerancia != null && Strings.isNotEmpty(par_tolerancia.getValor())) {
				
				if(par_tolerancia.getValor().contains(",")) {
					par_tolerancia.setValor(par_tolerancia.getValor().replace(",", "."));
				}
				
				try {
					tolerancia = Double.valueOf(par_tolerancia.getValor());
				}
				catch(Exception e) {
					throw new Exception("Erro ao converter o tipo do parâmetro de tolerância");
				}
			}
			
			int[] bufferDatabase = Conversao.comprimirAudio(audioStorage.recuperar(usuario.get().getNomeAudio()));
			
			int[] bufferRecebido = null;
			try {				
				bufferRecebido = Conversao.stringToInt(audioDto.getAudio());
			}
			catch(Exception e) {
				throw new Exception("Erro ao converter o tipo do áudio recebido");
			}
			
			boolean validacao = Algorithm.validate(tolerancia, bufferDatabase, bufferRecebido);
			
			if(validacao == false) {
				throw new UnauthorizedException("Senha falada não confere");
			}
			
			logServico.entrarPorta(usuario.get(), porta, dataHora, "falada");
						
			responseMensagem.setData(new UsuarioAcessoDto("Autorizado", usuario.get().getPessoa().getNome()));
			
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
