package br.com.utfpr.porta.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Lempel-Ziv-Welch (LZW) algorithm provides loss-less data compression
 * @author Abraham Lempel; Abraham Lempel; Terry Welch
 * @see http://rosettacode.org/wiki/LZW_compression
 */
public class LZW {
	
	private LZW() {}
	
    public static String decompress(List<String> compressed) {
            	
        Map<String,String> dictionary = new HashMap<>();
		for (int i = 48; i < 58; i++) { //0 - 9
			dictionary.put(String.valueOf((char) i), String.valueOf((char) i));
		}
		for (int i = 65; i < 71; i++) { //A - F
			dictionary.put(String.valueOf((char) i), String.valueOf((char) i));
		}
		
        String nextChar = "G"; //próximo caracter após o último índice do dicionário = G
         
        String w = compressed.remove(0);
        StringBuilder result = new StringBuilder(w);
        String entry;
        int aux;
        for (String k : compressed) {
            if (dictionary.containsKey(k)) {            	
            		entry = dictionary.get(k);
            }
            else if (k.compareTo(nextChar) == 0) {
                entry = w + w.charAt(0);
            } 
            else {
                throw new IllegalArgumentException("Bad compressed k: " + k);
            }
            result.append(entry);
 
            // Add w+entry[0] to the dictionary.
            dictionary.put(nextChar, w + entry.charAt(0));
            
            aux = (int) nextChar.charAt(0);
            if((aux + 1) == 92) { //Pular o caracter "\"
            		aux++;
            }
            nextChar = String.valueOf((char)++aux);
 
            w = entry;
        }
        return result.toString();
    }
}