package com.lg.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.lg.constant.ErrorMessageConstant;
import com.lg.exception.models.ErrorResponse;

/* Exceptions were handled in this class */
@ControllerAdvice
public class AuthenticationServiceExceptionHandler extends ResponseEntityExceptionHandler {

	/* Status code 400 is handled in this request */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String error = "Malformed JSON request";
		return buildResponseEntity(new ErrorResponse(HttpStatus.BAD_REQUEST, error));
	}

	private ResponseEntity<Object> buildResponseEntity(ErrorResponse responseError) {
		return new ResponseEntity<>(responseError, responseError.getStatus());
	}

	/*
	 * Document not found exception handled in this method
	 */
	@ExceptionHandler(UnExpectedLoginException.class)
	protected ResponseEntity<Object> handleDocumentNotFound(UnExpectedLoginException ex) {
		ErrorResponse responseError = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorMessageConstant.LOGIN_ISSUE);
		return buildResponseEntity(responseError);
	}

	/*
	 * Document conflict exception handled in this method
	 */
	@ExceptionHandler(UserAlreadyTakenException.class)
	protected ResponseEntity<Object> handleDocumentConflict(UserAlreadyTakenException ex) {
		ErrorResponse responseError = new ErrorResponse(HttpStatus.FOUND, ErrorMessageConstant.USERNAME_TAKEN);
		return buildResponseEntity(responseError);
	}

	@ExceptionHandler(UnauthorizedException.class)
	protected ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
		ErrorResponse responseError = new ErrorResponse(HttpStatus.UNAUTHORIZED,
				ErrorMessageConstant.UNAUTHORIZED_ERROR, ErrorMessageConstant.UNAUTHORISED_LOGIN_REASON);
		return buildResponseEntity(responseError);
	}

	@ExceptionHandler(SessionUnAuthorisedException.class)
	protected ResponseEntity<Object> handleSessionUnauthorizedException(SessionUnAuthorisedException ex) {
		ErrorResponse responseError = new ErrorResponse(HttpStatus.UNAUTHORIZED, "Not logged in");
		return buildResponseEntity(responseError);
	}
}