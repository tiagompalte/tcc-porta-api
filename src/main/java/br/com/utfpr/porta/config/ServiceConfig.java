package br.com.utfpr.porta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.com.utfpr.porta.servico.Servico;
import br.com.utfpr.porta.storage.AudioStorage;
import br.com.utfpr.porta.storage.local.AudioStorageLocal;
import br.com.utfpr.porta.storage.s3.AudioStorageS3;

@Configuration
@ComponentScan(basePackageClasses = Servico.class)
public class ServiceConfig {
	
	@Profile("local")
	@Bean
	public AudioStorage audioStorageLocal() {
		return new AudioStorageLocal();
	}
	
	@Profile("prod")
	@Bean
	public AudioStorage audioStorageS3() {
		return new AudioStorageS3();
	}

}
