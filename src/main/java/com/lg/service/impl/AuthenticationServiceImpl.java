package com.lg.service.impl;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lg.constant.AuthenticationConstants;
import com.lg.enums.ErrorCode;
import com.lg.exception.SessionUnAuthorisedException;
import com.lg.exception.UnExpectedLoginException;
import com.lg.exception.UnauthorizedException;
import com.lg.exception.UserAlreadyTakenException;
import com.lg.loginservice.CookieBase;
import com.lg.model.AuthenticationDTO;
import com.lg.model.RequestDTO;
import com.lg.model.UserResponseDTO;
import com.lg.repository.AuthenticationRepository;
import com.lg.service.AuthenticationService;
import com.lg.util.HttpUtil;

/**
 * {@link AuthenticationServiceImpl} is the implementation of
 * {@link AuthenticationService} for processing information about the Login and
 * Logout
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	@Value("${authenticationservice.url}")
	public String serviceUrl;

	@Value("${authenticationservice.userPrefix}")
	String createId;

	private JsonNode responseSettings;

	public JsonNode getResponseSettings() {
		return responseSettings;
	}

	CouchDbConnector couchDbConnector;
	String redirectUrl;

	@Autowired
	private CookieBase cookieBase;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	private CouchDbInstance couchDbInstance;
	@Autowired
	private AuthenticationRepository authenticationRepository;


	@PostConstruct
	public void initialise() {
		loadSettings();
	}

	/**
	 * Login and set the cookie in the header
	 */
	@Override
	public HttpServletResponse addLoginDetails(AuthenticationDTO request, HttpServletResponse httpResponse,
			HttpServletRequest httpRequest, String redirect) {
		String url = serviceUrl + AuthenticationConstants.SESSION_DATABASE;
		ResponseEntity<String> postResponseEntity;
		HttpHeaders headers = new HttpHeaders();
		HttpUtil httpUtil =  new HttpUtil();
		headers.add("Authorization", httpUtil.getBasicAuth(request));
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setName(request.getUser());
		requestDTO.setPassword(request.getPassword());
		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO, headers);
		redirect = redirect != null ? redirect : "";
		System.out.println("1111111111111111");

		try {
			postResponseEntity = getLoginSessionDetails(url, entity);
			System.out.println("postResponseEntity : " + postResponseEntity);
			String cookie = cookieBase.getCookieSession(postResponseEntity);
			System.out.println("cookie" + cookie);
			if (cookie == null) {
				logger.debug("URL {}", url);
				throw new SessionUnAuthorisedException(ErrorCode.SESSION_UNAUTHORIZED_ERROR);
			}
			logger.info("Session created");
			System.out.println("Session created");

			headers.add("Cookie", cookie);
			System.out.println("headers :" + headers);
			String getUserctxResponse = getSessionDetails(cookie);
			System.out.println("Session created **********");
			ResponseEntity<String> getUserctxResponseEntity = new ResponseEntity<>(getUserctxResponse, HttpStatus.OK);
			JsonNode sessionnode;
			System.out.println("************Session created **********");
			sessionnode = objectMapper.readTree(getUserctxResponse);
			headers.add("X-Medic-User", sessionnode.get("userCtx").get("name").asText());
			httpResponse.setContentType("text/html");
			redirectUrl = cookieBase.setCookie(getUserctxResponseEntity, httpResponse, httpRequest, redirect);
			httpResponse.sendRedirect(serviceUrl + redirectUrl);
		} catch (Exception ex) {
			System.out.println("Exception");
			throw new SessionUnAuthorisedException(ErrorCode.SESSION_UNAUTHORIZED_ERROR);
		}
		return httpResponse;
	}
	
	
	public ResponseEntity<String> getLoginSessionDetails(String url,HttpEntity<RequestDTO> entity){
		ResponseEntity<String> postResponseEntity;
		System.out.println("url : " + url + "entity : " + entity);
		postResponseEntity = restTemplate.postForEntity(url, entity, String.class);
		if (postResponseEntity.getStatusCodeValue() != 200) {
			throw new SessionUnAuthorisedException(ErrorCode.SESSION_UNAUTHORIZED_ERROR);
		}
		return postResponseEntity;
	}

	/**
	 * get the user details
	 */
	@Override
	public String getSessionDetails(String cookie) {
		logger.info("get the session details - service layer");
		HttpUtil httpUtil =  new HttpUtil();
		httpUtil.checkCookiee(cookie);
		String url = serviceUrl + AuthenticationConstants.SESSION_DATABASE;
	
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		if (response.getStatusCodeValue() != 200) {
			throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
		}
		return response.getBody();
	}

	/**
	 * delete the Login details
	 */
	@Override
	public String deleteSessionDetails(String cookie) {
		logger.info("delete the session details - service layer");
		HttpUtil httpUtil = new HttpUtil();
		httpUtil.checkCookiee(cookie);
		String url = serviceUrl + AuthenticationConstants.SESSION_DATABASE;
		ResponseEntity<String> response;
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		if (response.getStatusCodeValue() != 200) {
			throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
		}
		return response.getBody();
	}
	
	/*
	 * To get the User Details from medic and _user Database and process the user
	 * information
	 */
	public JsonNode userInfo(String db, String userName) {
		logger.info("get the user info - service layer");
		JsonNode response;
		couchDbConnector = couchDbInstance.createConnector(db, true);
		logger.debug("database {}",db);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		try {
			response = authenticationRepository.get(userName);
			if (response == null) {
				throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
			}
		} catch (Exception e) {
			throw new UnExpectedLoginException(ErrorCode.LOGIN_ISSUE);
		}

		return response;
	}

	/*
	 * To get the settings details
	 * information
	 */
	public JsonNode loadSettings() {
		logger.info("setting loading");
		String db = AuthenticationConstants.MEDIC_DB;
		couchDbConnector = couchDbInstance.createConnector(db, true);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode response = authenticationRepository.get(AuthenticationConstants.SETTINGS);
		UserResponseDTO.setResponseSetting(response);
		return UserResponseDTO.getResponseSetting();
	}


	/**
	 * To add details for admin user in medic/user database
	 */
	public JsonNode createUserSetting(JsonNode data, String db) {
		JsonNode settings = cookieBase.getSettingUpdates(data.get(AuthenticationConstants.USER_NAME).asText(), data);
		String settingId = createId + ":" + data.get(AuthenticationConstants.USER_NAME).asText();
		settings = ((ObjectNode) settings).put(AuthenticationConstants.DOCUMNENT_ID, settingId);
		couchDbConnector = couchDbInstance.createConnector(db, true);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		authenticationRepository.add(settings);
		JsonNode response = authenticationRepository.get(settingId);
		JsonNode result = response.get(AuthenticationConstants.REV_ID);
		result = ((ObjectNode) result).put(AuthenticationConstants.DOC_ID, settingId);
		return result;
	}
	

	/**
	 * To check user details are in the medic/user database for admin user
	 */
	public void validateNewUserNameForDb(String username, String db) {
		logger.info("check the user details - service layer");
		couchDbConnector = couchDbInstance.createConnector(db, true);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode response = authenticationRepository.get(createId + ":" + username);
		if (response.get(AuthenticationConstants.DOC_ID) != null) {
			throw new UserAlreadyTakenException(ErrorCode.USERNAME_TAKEN);
		} 
	}
	

	/**
	 * To check user details are in the medic/user database to update the language for the user
	 */
	public JsonNode validateUserOrSetting(String username, String db) {
		logger.info("check the user details to update the language - service layer");
		couchDbConnector = couchDbInstance.createConnector(db, true);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode response = authenticationRepository.get(createId + ":" + username);
		if (response.get(AuthenticationConstants.DOC_ID) != null) {
			throw new NotFoundException("Failed to find user.");
		} 
		return response;
	}
	

	/**
	 * To update the user language details in the medic/user database 
	 */
	public void updateUserOrSetting(JsonNode userOrSetting, String db) {
		logger.info("update the language details - service layer");
		couchDbConnector = couchDbInstance.createConnector(db, true);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		try {
		authenticationRepository.update(userOrSetting);
		}catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
		}
	}
	
	
	
}
