package com.kds.ourmemory.v1.advice.user.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class UserNotFoundException extends ArgsRuntimeException {

	public UserNotFoundException(Object... args) {
		super(args);
	}

}
