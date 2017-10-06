package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.utfpr.porta.modelo.Porta;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Portas;
import br.com.utfpr.porta.repositorio.Usuarios;
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
	
	@RequestMapping(value="/rfid/{rfid}/{codigo_porta}", method=RequestMethod.GET)
	public ResponseEntity<?> obterUsuarioPorRFID(@RequestHeader(value="time") String time, @PathVariable String rfid, @PathVariable Long codigo_porta) {
		
		if(StringUtils.isEmpty(rfid)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RFID não informado");
		}
		
		if(codigo_porta == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código da porta não informado");
		}
		
		if(Strings.isEmpty(time)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hora local não informada");
		}
		
		LocalDateTime dataHora;		
		try {
			dataHora = LocalDateTime.parse(time); //O formato deve ser, por exemplo: 2007-12-03T10:15:30
		}
		catch(Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hora local formatada incorretamente");
		}
		
		Usuario usuario = usuariosRepositorio.findByRfid(rfid);
		
		Porta porta = portasRepositorio.findOne(codigo_porta);
		
		if(usuario == null || porta == null) {
			return ResponseEntity.notFound().build();
		}
		
		if(!autorizacaoServico.validarAcessoUsuario(porta, usuario, dataHora)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário sem autorização para acesso a porta desejada");
		}
		
		logServico.entrarPorta(usuario, porta);
		
		return ResponseEntity.status(HttpStatus.OK).body(usuario);			
	}

}
