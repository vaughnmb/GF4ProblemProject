package com.iecokc.bom.model.exception;

public class WhereIsYourGodNowException extends RuntimeException {
	private static final long serialVersionUID = 723157874522473944L;

	public WhereIsYourGodNowException(Exception e) {
		super("This cannot happen, and you cannot recover", e);
	}
	
	public WhereIsYourGodNowException(String msg) {
		super(msg);
	}
}
