package br.com.utfpr.porta.config;

import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
	
	private static final String URL = "http://portaeletronica-api.herokuapp.com";
		
	/**
	 * Tarefa que executa a cada 25 minutos para a aplicação não hibernar
	 */
    @Scheduled(initialDelay = 0, fixedDelay = 1500000) //25 minutos
    public void heyApiNaoDurma() {
				
		try {
			URL obj = new URL(URL);
			HttpURLConnection con;
			int responseCode = 0;
			int tentativas = 0;
			do {			
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");			
				responseCode = con.getResponseCode();
				tentativas++;				
				Thread.sleep(3000);
			}
			while(responseCode != HttpURLConnection.HTTP_OK && tentativas < 10);
		}
		catch(Exception e) {
			LOGGER.error("Erro no ping no serviço de API: ", e);
		}
    }
    
}
