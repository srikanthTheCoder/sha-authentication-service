package com.lg.exception.models;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/* Error response DTO */
public class ErrorResponse {
	
	private String error;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String reason;
	@JsonIgnore
	private HttpStatus status;

	public ErrorResponse(HttpStatus status,String error,String reason) {
		this.error = error;
		this.status = status;
		this.reason = reason;
	}


	public ErrorResponse(HttpStatus status, String error) {
		this.status = status;
		this.error = error;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	
	public String getReason() {
		return reason;
	}


	public void setResponse(String reason) {
		this.reason = reason;
	}


	public String getError() {
		return error;
	}


	public void setError(String error) {
		this.error = error;
	}

}
