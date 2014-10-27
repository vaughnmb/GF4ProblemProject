package com.iecokc.bom.model.exception;

public class InvalidFileException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public InvalidFileException(String reason) {
		super(reason);
	}

}
