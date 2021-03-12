package com.kds.ourmemory.advice.exception;

public class CUserNotFoundException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CUserNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public CUserNotFoundException(String msg) {
		super(msg);
	}
	
	public CUserNotFoundException() {
		super();
	}
}
