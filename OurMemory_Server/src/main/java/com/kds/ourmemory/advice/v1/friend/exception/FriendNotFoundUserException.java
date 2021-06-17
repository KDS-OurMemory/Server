package com.kds.ourmemory.advice.v1.friend.exception;

public class FriendNotFoundUserException extends RuntimeException{
    public FriendNotFoundUserException(String msg) {
        super(msg);
    }
}
