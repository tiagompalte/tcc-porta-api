package br.com.utfpr.porta.controle;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.utfpr.porta.controle.dto.ErroDto;
import br.com.utfpr.porta.controle.dto.LogDto;
import br.com.utfpr.porta.controle.exception.BadRequestException;
import br.com.utfpr.porta.controle.exception.NotFoundException;
import br.com.utfpr.porta.modelo.Log;
import br.com.utfpr.porta.modelo.Usuario;
import br.com.utfpr.porta.repositorio.Logs;
import br.com.utfpr.porta.repositorio.Usuarios;
import br.com.utfpr.porta.response.Response;

@Controller
@RequestMapping("/api/logs")
public class LogControle {
	
	private static final Logger LOG = LoggerFactory.getLogger(LogControle.class);
	private static final String EMAIL_USUARIO = "username";
	
	@Autowired
	private Usuarios usuarioRepositorio;
	
	@Autowired
	private Logs logRepositorio;
		
	/**
	 * Obter lista de logs. Ordenado pela data de inserção do log
	 * @param page
	 * @param linesPerPage
	 * @param direction (ASC / DESC (default))
	 * @param request
	 * @return lista de logs
	 */
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity obterLogsPorPeriodo(
			@RequestParam(value="page", defaultValue="0") Integer page, 
			@RequestParam(value="linesPerPage", defaultValue="10") Integer linesPerPage,
			@RequestParam(value="direction", defaultValue="DESC") String direction,			
			HttpServletRequest request) {
		
		Response<ErroDto> responseErro = new Response<>();
		Page<LogDto> listDto = null;
		
		try {
			
			if(request.getAttribute(EMAIL_USUARIO) == null) {
				throw new BadRequestException("E-mail do usuário nao informado");
			}
						
			Optional<Usuario> usuario = usuarioRepositorio.findByEmail(request.getAttribute(EMAIL_USUARIO).toString());
			
			if(!usuario.isPresent()) {
				throw new NotFoundException("Usuario nao encontrado");
			}
			
			if(usuario.get().getEstabelecimento() == null || usuario.get().getEstabelecimento().getCodigo() == null) {
				throw new BadRequestException("Usuario nao e responsavel por nenhum estabelecimento");
			}
			
			LocalDateTime dataHoraInicio = LocalDateTime.MIN;
			LocalDateTime dataHoraFim = LocalDateTime.now();
			
			Pageable pageable = new PageRequest(page, linesPerPage, Direction.fromStringOrNull(direction), "dataHora");
			
			Page<Log> pageLogs = logRepositorio.filtrar(usuario.get().getEstabelecimento(), dataHoraInicio, dataHoraFim, pageable);
			
			listDto = pageLogs.map(log -> new LogDto(log));
			
		}
		catch(BadRequestException e) {
			LOG.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseErro);			
		}
		catch(NotFoundException e) {
			LOG.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErro);	
		}
		catch(Exception e) {
			LOG.error(e.getMessage());
			responseErro.setData(new ErroDto(e.getMessage()));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseErro);
		}
		
		return ResponseEntity.ok().body(listDto);
	}

}
