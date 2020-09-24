package com.lg.exception;

import com.lg.enums.ErrorCode;


/**
 * {@link SessionUnAuthorisedException} class for handle all the UnAuthorised Exception
 * while Login at runtime exception
 */
public class SessionUnAuthorisedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;

	public SessionUnAuthorisedException(ErrorCode errorCode) {
		super(errorCode.getError());
		this.errorCode = errorCode;
	}
}
