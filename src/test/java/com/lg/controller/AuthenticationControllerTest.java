package com.lg.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.lg.model.AuthenticationDTO;
import com.lg.service.AuthenticationService;

@Ignore
class AuthenticationControllerTest {

	@Mock
	private AuthenticationService authenticationService;
	
	@Mock
	HttpServletResponse response;
	
	@Mock
	HttpServletRequest request;

	@InjectMocks
	private AuthenticationController authenticationController;

	public MockMvc mockMvc;
	
	
	@BeforeEach
	void setUp() throws Exception {
		authenticationController = new AuthenticationController();
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .build();
		MockitoAnnotations.initMocks(this);
		response.setStatus(302);
	}

	@Test
	void testAddSessionDetails() {
		AuthenticationDTO req = new AuthenticationDTO();
		req.setUser("subhi");
		req.setPassword("C7KrKxFfu3uUy2Kw");
		String redirect = "";
		when(authenticationService.addLoginDetails(req, response, request, redirect)).thenReturn(response);
		authenticationController.addSessionDetails(req, redirect, response, request);
		Mockito.verify(authenticationService, times(1)).addLoginDetails(req, response, request, redirect);
	}

	@Test
	void testGetSessionDetails() {
		String cookie = "test";
		String expectedResponse = "Success";
		when(authenticationService.getSessionDetails(cookie)).thenReturn(expectedResponse);
		ResponseEntity<String> actualResponse = authenticationController.getSessionDetails(cookie);
		assertEquals(actualResponse.getBody(),expectedResponse);
		assertEquals(HttpStatus.SC_OK, actualResponse.getStatusCodeValue());
	}

	@Test
	void testDeleteSessionDetails() {
		String cookie = "test";
		String expectedResponse = "Success";
		when(authenticationService.deleteSessionDetails(cookie)).thenReturn(expectedResponse);
		ResponseEntity<String> actualResponse = authenticationController.deleteSessionDetails(cookie);
		assertEquals(actualResponse.getBody(),expectedResponse);
		assertEquals(HttpStatus.SC_OK, actualResponse.getStatusCodeValue());
	}

}
