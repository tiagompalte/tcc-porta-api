package br.com.utfpr.porta.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

@Configuration
@PropertySource(value = { "file:\\${USERPROFILE}\\.porta-s3.properties", "file://${HOME}/.porta-s3.properties" }, ignoreResourceNotFound = true)
public class S3Config {
	
	@Autowired
	private Environment env;
	
	private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
	private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
			
	@Profile("local")
	@Bean
	public AmazonS3 amazonS3Local() {
		
		if(StringUtils.isEmpty(env.getProperty(AWS_ACCESS_KEY_ID)) || StringUtils.isEmpty(env.getProperty(AWS_SECRET_ACCESS_KEY))) {
			return null;
		}
				
		return amazonS3(env.getProperty(AWS_ACCESS_KEY_ID), env.getProperty(AWS_SECRET_ACCESS_KEY));
	}

	@Profile("prod")
	@Bean
	public AmazonS3 amazonS3Prod() {
						
		return amazonS3(System.getenv(AWS_ACCESS_KEY_ID), System.getenv(AWS_SECRET_ACCESS_KEY));
	}
	
	private AmazonS3 amazonS3(String id, String access) {
		AWSCredentials credenciais = new BasicAWSCredentials(id, access);
		AmazonS3 amazonS3 = new AmazonS3Client(credenciais, new ClientConfiguration());
		Region regiao = Region.getRegion(Regions.US_EAST_1);
		amazonS3.setRegion(regiao);
		return amazonS3;
	}

}
