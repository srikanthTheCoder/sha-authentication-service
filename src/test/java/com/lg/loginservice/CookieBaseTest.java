package com.lg.loginservice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


class CookieBaseTest {

	@InjectMocks
	private CookieBase cookieBase;


	@BeforeEach
	void setUp() throws Exception {
		cookieBase = new CookieBase();
	}

	@Test
	void should_return_CookieDetails_on_getCookieSession() {
		String cookie = "AuthSession=c3ViaGk6NUY2Q0RCMzY6A5BraOcQxJ8T9l1iw9sMUe0Wyuw; Version=1; Expires=Fri, 24-Sep-2021 17:45:26 GMT; Max-Age=31536000; Path=/; HttpOnly";
		HttpHeaders headers = new HttpHeaders();
		List<String> s = new ArrayList<>();
		s.add(cookie);
		headers.addAll("Set-Cookie", s);
		ResponseEntity<String> response = new ResponseEntity<>(headers, HttpStatus.OK);
		String actualCookie = cookieBase.getCookieSession(response);
		assertEquals(cookie, actualCookie);
	}

}
