package com.lg.exception;

import com.lg.enums.ErrorCode;


/**
 * {@link UserAlreadyTakenException} class for handle all the User Found Exception
 * while creating new admin user at runtime exception
 */
public class UserAlreadyTakenException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;

	public UserAlreadyTakenException(ErrorCode errorCode) {
		super(errorCode.getError());
		this.errorCode = errorCode;
	}

}
