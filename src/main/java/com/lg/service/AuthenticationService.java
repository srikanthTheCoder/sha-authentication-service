package com.lg.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.lg.model.AuthenticationDTO;


/**
 * {@link AuthenticationService} handles the Login and Logout
 */
@Service
public interface AuthenticationService {
	 /**
     * Get the User Information
     *
     * @param cookie - Cookie used for getting User Information
     * @return String as User information
     */
	String getSessionDetails(String cookie);

	 /**
     * Get the Logout Information
     *
     * @param cookie - Cookie used for getting Logout Information
     * @return String as Logout information
     */
	String deleteSessionDetails(String cookie);
	
	 /**
     * Get the Login Information
     *
     * @param cookie - Cookie used for getting Login Information
     * @return HttpServletResponse as Redirect URL and Cookie information
     */
	HttpServletResponse addLoginDetails(AuthenticationDTO request, HttpServletResponse httpResponse,
			HttpServletRequest httpRequest, String redirect);
}
