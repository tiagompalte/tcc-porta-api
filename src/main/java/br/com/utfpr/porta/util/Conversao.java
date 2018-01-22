package br.com.utfpr.porta.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;

public class Conversao {
	
	public static byte[] convertWavToAlaw(byte[] sound) throws IOException, UnsupportedAudioFileException {		
		AudioInputStream ais = toStream(sound);
		ais = convertAsStream(ais);	
		return toByteArray(ais);
	}
		
	private static AudioInputStream toStream(byte[] bytes) throws IOException, UnsupportedAudioFileException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
		return ais;
	}
	
	private static AudioInputStream convertAsStream(AudioInputStream sourceStream) {	
		
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioInputStream targetStream = null;

		if (!AudioSystem.isConversionSupported(Encoding.ALAW, sourceFormat)) {
			AudioFormat intermediateFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), 2 * sourceFormat.getChannels(), // frameSize
					sourceFormat.getSampleRate(), false);

			if (AudioSystem.isConversionSupported(intermediateFormat, sourceFormat)) {				
				sourceStream = AudioSystem.getAudioInputStream(intermediateFormat, sourceStream);
			}
		}

		targetStream = AudioSystem.getAudioInputStream(Encoding.ALAW, sourceStream);

		if (targetStream == null) {
			throw new RuntimeException("Audio conversion not supported");
		}

		return targetStream;
	}
	
	private static byte[] toByteArray(AudioInputStream ais) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AudioSystem.write(ais, Type.WAVE, baos);
        return baos.toByteArray();
	}

}
