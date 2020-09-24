package com.lg.exception;

import com.lg.enums.ErrorCode;

/**
 * {@link UnExpectedLoginException} class for handle all the UnExcepted Exception
 * while Login, getting User info and Logout at runtime exception
 */
public class UnExpectedLoginException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;

	public UnExpectedLoginException(ErrorCode errorCode) {
		super(errorCode.getError());
		this.errorCode = errorCode;
	}



}
