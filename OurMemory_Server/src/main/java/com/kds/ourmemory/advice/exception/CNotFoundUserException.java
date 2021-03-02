package com.kds.ourmemory.advice.exception;

public class CNotFoundUserException extends RuntimeException{
	public CNotFoundUserException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public CNotFoundUserException(String msg) {
		super(msg);
	}
	
	public CNotFoundUserException() {
		super();
	}
}
