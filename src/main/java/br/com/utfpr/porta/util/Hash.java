package br.com.utfpr.porta.util;

import java.security.MessageDigest;

public class Hash {
	
	public static String MD5(String message) throws Exception{ 
		MessageDigest md = MessageDigest.getInstance("MD5"); 
		byte[] hash = md.digest(message.getBytes("UTF-8")); 
		StringBuilder sb = new StringBuilder(2*hash.length); 
		for(byte b : hash){ 
			sb.append(String.format("%02x", b&0xff)); 
		} 
		return sb.toString();	
	}

}
