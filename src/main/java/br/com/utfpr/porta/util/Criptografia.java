package br.com.utfpr.porta.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Criptografia {
	
	private static Charset CHARSET = Charset.forName("ASCII");
	
	public static String decode(String input, String chave) throws UnsupportedEncodingException {
		
		if(input == null) {
			throw new NullPointerException("Input não informado");
		}
		
		if(chave == null) {
			throw new NullPointerException("Chave não informada");
		}
		
		chave = detectorLetra(chave);
		
		if(input.length() > chave.length()) {
			
			for(int i = chave.length(); i < input.length(); i++) {
				chave = chave.concat("T");
			}
			
		}
		
		byte[] output = new byte[128];				
		byte[] input_array = input.getBytes(CHARSET);
		byte[] chave_array = chave.getBytes(CHARSET);
						
		for(int i = 0; i < input.length(); i++) {
			output[i] = (byte)(input_array[i] - 30 + 159 - 32 - chave_array[i]);
		}
		
		String retorno = new String(output, CHARSET);
						
		return retorno.trim();
	}
	
	private static String detectorLetra(String chave) {
		
		byte[] chave_array = chave.getBytes(CHARSET);
	    
	    for(int i = 0; i < chave_array.length; i++){
	        
	        if (chave_array[i] > 97) {
	        	chave_array[i] = (byte)(chave_array[i] - 32);
	        }	        
	        
	    }
	    
	    chave = new String(chave_array, Charset.forName("ASCII"));
	    
	    return chave;
	}
	
}