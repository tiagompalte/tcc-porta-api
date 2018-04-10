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
import javax.sound.sampled.AudioFileFormat.Type;

public class Conversao {
	
	public static String convertHexToDecimal(String hex, boolean invertido) throws Exception {
		
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
		
		int numero;
		try {
			numero = Integer.parseInt(concatenar.toString(), 16);
		}
		catch(NumberFormatException e) {
			throw new Exception("Erro ao converter código hexadecimal em decimal. ".concat(e.getMessage()));
		}
		
		return String.valueOf(numero);
	}
	
	public static int[] comprimirAudio(byte[] audio_byte) throws Exception {
				
		int[] audio = null;
		try {			
						
			if(audio_byte == null) {
				throw new NullPointerException("Áudio não pode ser recuperado");
			}
			
			AudioInputStream audioInput = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audio_byte));				
			AudioFormat audioFormat = new AudioFormat(16000, 8, 1, true, audioInput.getFormat().isBigEndian());				
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
			throw new Exception("Erro ao codificar o áudio. ".concat(e.getMessage()));
		}
		
		return audio;
	}	
	
	public static int[] leituraArquivoTXT(Path arquivo) throws Exception {
				
		int[] int_array = null;
		try {			
						
			if(arquivo == null) {
				throw new NullPointerException("Nenhum arquivo recebido");
			}
			
			List<String> linhas_arquivo = Files.readAllLines(arquivo);
			int_array = new int[linhas_arquivo.size()];
			for(int i = 0; i < linhas_arquivo.size(); i++) {
				int_array[i] = Integer.parseInt(linhas_arquivo.get(i));
			}
		} catch(Exception e) {
			throw new Exception("Erro ao converter array. ".concat(e.getMessage()));
		}
		
		return int_array;
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
	
//	public static float[] intToFloat(int[] ints) {
//		float[] floaters = new float[ints.length];
//		for (int i = 0; i < ints.length; i++) {
//	        floaters[i] = ints[i];
//	    }
//	    return floaters;
//	}
	
	public static int[] stringToInt(String chars) {
		int[] ints = new int[chars.length()];
		for (int i = 0; i < chars.length(); i++) {
	        ints[i] = (int) chars.charAt(i);
	    }
	    return ints;
	}

}
