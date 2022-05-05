package com.kds.ourmemory.user.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class UserNotFoundException extends ArgsRuntimeException {

	public UserNotFoundException(Object... args) {
		super(args);
	}

}
