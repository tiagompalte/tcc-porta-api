package br.com.utfpr.porta.config;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import br.com.utfpr.porta.controle.Controle;

@Configuration
@ComponentScan(basePackageClasses = { Controle.class })
@EnableWebMvc
@EnableSpringDataWebSupport
public class WebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {
	
	@SuppressWarnings("unused")
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
		
	@Bean
	public FormattingConversionService mvcConversionService() {		
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		return conversionService;
	}
		
	@Bean
	public LocaleResolver localeResolver() {
		return new FixedLocaleResolver(new Locale("pt", "BR"));
	}
	
}
