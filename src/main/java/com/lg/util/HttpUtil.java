package com.lg.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.lg.constant.AuthenticationConstants;
import com.lg.enums.ErrorCode;
import com.lg.exception.UnauthorizedException;
import com.lg.model.AuthenticationDTO;

@Component
public class HttpUtil {

	@Value("${authenticationservice.accept}")
	public String accept;


	/**
	 * set the cookie in the http header
	 */
	public HttpEntity<String> getHttpEntity(String cookie) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Cookie", cookie);
		httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(httpHeaders);
	}


	/**
	 * get the basic authentication
	 */
	public String getBasicAuth(AuthenticationDTO request) {
		Base64.Encoder encoder = Base64.getEncoder();
		String auth = request.getUser() + ":" + request.getPassword();
		return "Basic " + encoder.encodeToString(auth.getBytes());
	}


	/**
	 * set the cookie parameters in the cookie
	 */
	public Cookie setCookieParam(String name, String value) {
		Cookie cookie = null;
		try {
			cookie = new Cookie(name, URLEncoder.encode(value, "UTF-8"));
			cookie.setMaxAge(31536000);
			cookie.setPath(AuthenticationConstants.PATH_SEPERATOR);
			cookie.setSecure(true);
			cookie.setHttpOnly(false);
			return cookie;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cookie;
	}
	
	public void checkCookiee(String cookie) {
		if (cookie.equals(AuthenticationConstants.MISSING)) {
			throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
		}
	}


}
