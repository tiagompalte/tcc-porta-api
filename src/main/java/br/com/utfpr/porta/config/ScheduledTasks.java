package br.com.utfpr.porta.config;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.utfpr.porta.modelo.Parametro;
import br.com.utfpr.porta.repositorio.Parametros;

@Component
public class ScheduledTasks {
	
	private static final String PARAMETRO_API = "URL_API";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
		
	@Autowired
	private Parametros parametroRepositorio;
		
	/**
	 * Tarefa que executa a cada 25 minutos para a aplicação não hibernar
	 */
    @Scheduled(initialDelay = 0, fixedDelay = 1500000) //25 minutos
    public void heyApiNaoDurma() {
    	
    		Parametro parUrlApi = parametroRepositorio.findOne(PARAMETRO_API);
		
		if(parUrlApi == null || Strings.isEmpty(parUrlApi.getValor())) {
			LOGGER.error("Erro no ping no serviço API. Parâmetro {} não cadastrado", PARAMETRO_API);
			return;
		}
				
		try {
			URL obj = new URL(parUrlApi.getValor());
			HttpURLConnection con;
			int responseCode = 0;
			int tentativas = 0;
			do {			
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");			
				responseCode = con.getResponseCode();
				tentativas++;
				LOGGER.info("Ping no serviço de API: Tentativa: {} Resposta: {}", tentativas, responseCode);
				Thread.sleep(3000);
			}
			while(!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) && tentativas < 10);
		}
		catch(Exception e) {
			LOGGER.error("Erro no ping no serviço de API: ", e);
		}
    }
    
}
