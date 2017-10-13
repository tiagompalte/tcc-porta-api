package br.com.utfpr.porta.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class CriptogragiaTeste {

	@Test
	public void testeDecode() {
		
		String mensagem = "AzTAUANmarinho";//Mensagem
        String chave = "AzSJFHSJFBSJFHSJ";//Chave LETRA MAIUSCULA SEMPRE        
        String mensagem_criptografada = "!sF*:(@VFS[WMV";
        
		String retorno = "";
		try {
			retorno = Criptografia.decode(mensagem_criptografada, chave);
			assertEquals(mensagem, retorno);
		} catch (UnsupportedEncodingException e) {			
			fail(e.getMessage());
		}
				
	}

}
