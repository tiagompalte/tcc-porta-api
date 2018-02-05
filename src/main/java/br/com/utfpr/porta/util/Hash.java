package br.com.utfpr.porta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.logging.log4j.util.Strings;

public class Hash {
	
	public enum Algorithm {
		MD5("MD5"),
		SHA("SHA"),
		SHA_1("SHA-1"),
		SHA_256("SHA-256"),
		SHA_384("SHA-384"),
		SHA_512("SHA-512");
		
		private String algorithm;
		
		private Algorithm(String algorithm) {
			this.algorithm = algorithm;
		}
		
		public String getAlgorithm() {
			return algorithm;
		}
	}
	
	public static String gerarHash(String message, Algorithm algorithm) throws Exception { 
		
		if(Strings.isEmpty(message)) {
			throw new Exception("Mensagem não informada");
		}
		
		if(algorithm == null) {
			throw new Exception("Tipo do algoritmo não informado");
		}
		
		MessageDigest md = MessageDigest.getInstance(algorithm.getAlgorithm()); 
		byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder(2*hash.length); 
		for(byte b : hash){ 
			sb.append(String.format("%02x", b&0xff)); 
		} 
		return sb.toString();	
	}
	
}
