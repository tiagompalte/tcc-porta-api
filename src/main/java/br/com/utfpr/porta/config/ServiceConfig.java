package br.com.utfpr.porta.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import br.com.utfpr.porta.servico.Servico;

@Configuration
@ComponentScan(basePackageClasses = Servico.class)
public class ServiceConfig {

}
