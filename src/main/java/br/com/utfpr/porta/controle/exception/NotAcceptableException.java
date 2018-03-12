package br.com.utfpr.porta.controle.exception;

public class NotAcceptableException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public NotAcceptableException(String msg) {
		super(msg);
	}

}
