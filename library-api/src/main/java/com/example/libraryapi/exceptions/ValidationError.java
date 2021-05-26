package com.example.libraryapi.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError {


	private static final long serialVersionUID = 1L;
	
	private List<FieldMessage> erros = new ArrayList<FieldMessage>();
	

	public ValidationError(Instant timestamp, Integer status, String error, String message, String path) {
		super(timestamp, status, error, message, path);
	}


	public void setErros(List<FieldMessage> erros) {
		this.erros = erros;
	}


	public List<FieldMessage> getErros() {
		return erros;
	}


	public void addError(String fieldName,String mensagem) {
		erros.add(new FieldMessage(fieldName, mensagem));
	}
	
	
	
}
