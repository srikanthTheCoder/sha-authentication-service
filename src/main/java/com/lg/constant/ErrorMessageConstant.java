package com.lg.constant;

import java.io.Serializable;

public class ErrorMessageConstant implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String ERRORCODE_001 = "ERROR_001";
	public static final String ERRORCODE_002 = "ERROR_002";
	public static final String ERRORCODE_003 = "ERROR_003";
	public static final String BAD_REQUEST_ERROR = "Malformed JSON request";
	public static final String UNAUTHORIZED_ERROR = "unauthorized";
	public static final String UNAUTHORISED_LOGIN_REASON = "Authentication required.";
	public static final String LOGIN_ISSUE = "Unexpected error logging in";
	public static final String INVALID_USER ="Username is not valid";
	public static final String USERNAME_TAKEN = "Username already taken";
	public static final String ILLEGAL_ATTEMPT = "illegal attempt";
	
}
