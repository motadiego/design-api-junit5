package com.example.libraryapi.exceptions;

public class BusinnesException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public BusinnesException() {
		
	}
	
	public BusinnesException(String msg) {
		super(msg);
	}
}
