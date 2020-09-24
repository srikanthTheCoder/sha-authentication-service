package com.lg.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.lg.constant.AuthenticationConstants;
import com.lg.model.AuthenticationDTO;
import com.lg.service.AuthenticationService;

/**
 * Controller to access the Login and Logout details
 */

@RestController
public class AuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

	@Autowired
	private AuthenticationService authenticationService;

	/**
	 * To Login the application
	 */
	@PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public void addSessionDetails(@RequestBody AuthenticationDTO request,
			@QueryParam(AuthenticationConstants.REDIRECT) String redirect, HttpServletResponse httpResponse,
			HttpServletRequest httpRequest) {
		logger.info("Login Session");
		HttpServletResponse loginResponse;
		loginResponse = authenticationService.addLoginDetails(request, httpResponse, httpRequest, redirect);
		loginResponse.setStatus(302);
	}

	/**
	 * To get the user details
	 */
	@GetMapping(value = "/_session", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getSessionDetails(
			@RequestHeader(name = AuthenticationConstants.COOKIE, defaultValue = AuthenticationConstants.MISSING) String cookie) {
		logger.info("User Details");
		return new ResponseEntity<>(authenticationService.getSessionDetails(cookie), HttpStatus.OK);
	}

	/**
	 * To Logout the application
	 */
	@DeleteMapping(value = "/_session", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteSessionDetails(
			@RequestHeader(name = AuthenticationConstants.COOKIE, defaultValue = AuthenticationConstants.MISSING) String cookie) {
		logger.info("Logout Session");
		return new ResponseEntity<>(authenticationService.deleteSessionDetails(cookie), HttpStatus.OK);
	}
}
