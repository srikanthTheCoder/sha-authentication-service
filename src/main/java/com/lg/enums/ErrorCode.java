package com.lg.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED, "unauthorized","Authentication required."),
    SESSION_UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED, "Not logged in"),
    USERNAME_TAKEN(HttpStatus.FOUND, "Username already taken"),
    LOGIN_ISSUE(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error logging in"),
    ILLEGAL_ATTEMPT(HttpStatus.INTERNAL_SERVER_ERROR, "illegal attempt"),
    INVALID_USER(HttpStatus.NOT_FOUND, "Username is not valid"),
    BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "Malformed JSON request");

    private final HttpStatus status;
    private final String error;
    private String reason;

    ErrorCode(HttpStatus status, String error,String reason) {
        this.status = status;
        this.error = error;
		this.reason = reason;
    }

    ErrorCode(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }

	public String getError() {
		return error;
	}

}
