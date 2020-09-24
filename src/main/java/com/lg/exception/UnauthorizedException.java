package com.lg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.lg.enums.ErrorCode;


/**
 * {@link UnauthorizedException} class for handle all the UnAuthorised Exception
 * while getting User info and Logout at runtime exception
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;

	public UnauthorizedException(ErrorCode errorCode) {
		super(errorCode.getError());
		this.errorCode = errorCode;
	}

}
