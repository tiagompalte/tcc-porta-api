import static java.nio.file.FileSystems.getDefault;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import br.com.utfpr.porta.util.Algorithm;
import br.com.utfpr.porta.util.Conversao;

public class AudioTest {
	
	private static Path PATH_AUDIOS = getDefault().getPath(System.getenv("USERPROFILE"), ".portaaudios");
	private static String NOME_ARQ_FILE_DATABASE = "";
	private static String NOME_ARQ_FILE_RECEBIDO = "";
	
	private int[] bufferDatabase;
	private int[] bufferRecebido;	
	
	@Before
	public void beforeTest() {
				
		try {
			bufferDatabase = Conversao.comprimirAudio(
					Files.readAllBytes(PATH_AUDIOS.resolve(NOME_ARQ_FILE_DATABASE)));
		} catch (Exception e) {
			fail("Erro ao carregar arquivo referente ao áudio do DATABASE ".concat(e.getMessage()));
		}
		
		try {
			bufferRecebido = Conversao.leituraArquivoTXT(PATH_AUDIOS.resolve(NOME_ARQ_FILE_RECEBIDO));
		} catch (Exception e) {
			fail("Erro ao carregar arquivo referente ao áudio RECEBIDO ".concat(e.getMessage()));
		}
				
	}

	@Test
	public void validarAudio() {
		
		boolean validacao = Algorithm.validate(bufferDatabase, bufferRecebido);
		
		assertTrue(validacao);
	}

}
