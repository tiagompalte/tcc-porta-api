package br.com.utfpr.porta.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.logging.log4j.util.Strings;

import br.com.utfpr.porta.controle.exception.ConversaoException;

import javax.sound.sampled.AudioFileFormat.Type;

public class Conversao {
	
	private Conversao() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String convertHexToDecimal(String hex, boolean invertido) {
		
		if(hex == null || hex.isEmpty()) {
			throw new NullPointerException("Código hexadecimal não informado");
		}
		
		String[] regex = hex.split("\\s");
		StringBuilder concatenar = new StringBuilder();
		
		if(invertido) {
			for(int i = regex.length-1; i >= 0; i--) {
				concatenar.append(regex[i]);
			}
		}
		else {
			for(int i = 0; i < regex.length; i++) {
				concatenar.append(regex[i]);
			}
		}
		
		Long numero;
		try {
			numero = Long.parseLong(concatenar.toString(), 16);
		}
		catch(NumberFormatException e) {
			throw new ConversaoException("Erro ao converter código hexadecimal em decimal. ".concat(e.getMessage()));
		}
		
		return String.valueOf(numero);
	}
	
	public static int[] comprimirAudio(byte[] audioByte) {
				
		int[] audio = null;
		try {			
						
			if(audioByte == null) {
				throw new NullPointerException("Áudio não pode ser recuperado");
			}
			
			AudioInputStream audioInput = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioByte));				
			AudioFormat audioFormat = new AudioFormat(8000, 8, 1, false, audioInput.getFormat().isBigEndian());				
			AudioInputStream outStream = AudioSystem.getAudioInputStream(audioFormat, audioInput);								
			File tempFile = File.createTempFile("audio", ".temp");
			AudioSystem.write(outStream, Type.WAVE, tempFile);				
			byte[] tempByte = Files.readAllBytes(tempFile.toPath());
			
			//Inicia do byte 44 para pular o metadata do arquivo wav				
			audio = new int[tempByte.length - 44];
			for(int i = 0; i < tempByte.length - 44; i++) {
				audio[i] = tempByte[i + 44] + 128;
			}
		} catch(Exception e) {
			throw new ConversaoException("Erro ao codificar o áudio. ".concat(e.getMessage()));
		}
		
		return audio;
	}	
	
	public static int[] leituraArquivoTXT(Path arquivo) {
				
		int[] intArray = null;
		try {			
						
			if(arquivo == null) {
				throw new NullPointerException("Nenhum arquivo recebido");
			}
			
			List<String> linhasArquivo = Files.readAllLines(arquivo);
			intArray = new int[linhasArquivo.size()];
			for(int i = 0; i < linhasArquivo.size(); i++) {
				intArray[i] = Integer.parseInt(linhasArquivo.get(i));
			}
		} catch(Exception e) {
			throw new ConversaoException("Erro ao converter array. ".concat(e.getMessage()));
		}
		
		return intArray;
	}
		
	public static float[] shortToFloat(short[] pcms) {
	    float[] floaters = new float[pcms.length];
	    for (int i = 0; i < pcms.length; i++) {
	        floaters[i] = pcms[i];
	    }
	    return floaters;
	}
	
	public static short[] byteToShort(byte[] bytes) {
	    short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
	    ByteBuffer bb = ByteBuffer.wrap(bytes);
	    for (int i = 0; i < out.length; i++) {
	        out[i] = bb.getShort();
	    }
	    return out;
	}
		
	public static int[] stringToInt(String chars) {
		int[] ints = new int[chars.length()];
		for (int i = 0; i < chars.length(); i++) {
	        ints[i] = (int) chars.charAt(i);
	    }
	    return ints;
	}
	
	public static int[] hexToInt(String hex) {
		if(Strings.isEmpty(hex)) {
			throw new NullPointerException();
		}
		int[] ints = new int[hex.length()/2];
		for(int i = 0; i < ints.length; i++) {
			ints[i] = Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
		}
		return ints;
	}
	
	public static double stringToDouble(String texto, String mensagemErro) {
		
		if(Strings.isEmpty(texto)) {
			throw new NullPointerException();
		}
		
		if(texto.contains(",")) {
			texto = texto.replace(",", ".");
		}
		
		try {
			return Double.valueOf(texto);
		}
		catch(Exception e) {
			throw new ConversaoException(Strings.isNotEmpty(mensagemErro) ? mensagemErro : "Erro de conversao");
		}
	}

}
