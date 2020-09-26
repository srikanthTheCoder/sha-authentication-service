package com.lg.loginservice;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.lg.constant.AuthenticationConstants;
import com.lg.service.impl.AuthenticationServiceImpl;

class CookieUserTest {

	@Mock
	private AuthenticationServiceImpl authenticationServiceImpl;

	@InjectMocks
	public CookieUser cookieUser;

	public MockMvc mockMvc;

	private static Map<String, List<String>> roles = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		cookieUser = new CookieUser();
		mockMvc = MockMvcBuilders.standaloneSetup(cookieUser).build();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testValidateNewUserName() {
		String username = "user01";
		JsonNode value = null;
		when(authenticationServiceImpl.validateNewUserNameForDb(username, AuthenticationConstants.USER_DB))
				.thenReturn(value);
		when(authenticationServiceImpl.validateNewUserNameForDb(username, AuthenticationConstants.MEDIC_DB))
				.thenReturn(value);
		cookieUser.validateNewUserName(username);
		Mockito.verify(authenticationServiceImpl, times(1)).validateNewUserNameForDb(username,
				AuthenticationConstants.USER_DB);
		Mockito.verify(authenticationServiceImpl, times(1)).validateNewUserNameForDb(username,
				AuthenticationConstants.MEDIC_DB);
	}

}
