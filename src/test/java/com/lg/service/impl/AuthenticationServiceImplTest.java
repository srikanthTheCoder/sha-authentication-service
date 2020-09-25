package com.lg.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lg.constant.AuthenticationConstants;
import com.lg.enums.ErrorCode;
import com.lg.exception.SessionUnAuthorisedException;
import com.lg.exception.UnauthorizedException;
import com.lg.loginservice.CookieBase;
import com.lg.model.AuthenticationDTO;
import com.lg.model.RequestDTO;
import com.lg.repository.AuthenticationRepository;
import com.lg.util.HttpUtil;

@ExtendWith({ MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceImplTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private AuthenticationRepository authenticationRepository;

	@Mock
	HttpServletResponse httpResponse;

	@Mock
	HttpServletRequest httpRequest;

	@Mock
	private CouchDbInstance couchDbInstance;

	@Mock
	CouchDbConnector couchDbConnector;

	@InjectMocks
	private AuthenticationServiceImpl authenticationServiceImpl;

	@Mock
	private CookieBase cookieBase;

	public MockMvc mockMvc;
	private HttpUtil httpUtil;

	@BeforeEach
	void setUp() throws Exception {
		authenticationServiceImpl = new AuthenticationServiceImpl();
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationServiceImpl).build();
		httpUtil = new HttpUtil();
		ReflectionTestUtils.setField(authenticationServiceImpl, "serviceUrl", "https://smarthealth-subhi.lg-apps.com/");
		ReflectionTestUtils.setField(authenticationServiceImpl, "createId", "org.couchdb.user");
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testGetSessionDetails() {
		String cookie = "test";
		String expectedResponse = "Success";
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.GET, entity, String.class))
						.thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));
		String actualResponse = authenticationServiceImpl.getSessionDetails(cookie);
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	void testGetSessionDetailsThrowError() {
		String cookie = "test";
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		ResponseEntity<String> value = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.GET, entity, String.class)).thenReturn(value);
		Exception exception = assertThrows(UnauthorizedException.class, () -> {
			authenticationServiceImpl.getSessionDetails(cookie);
		});
		assertEquals("unauthorized", exception.getMessage());

	}
	
	// @Test
	void testAddLoginDetails_throwSetssionUnAuthorisedException1() throws IOException {
		String url = authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE;
		String redirect = "";
		String cookie = "AuthSession=c3ViaGk6NUY2Q0RCMzY6A5BraOcQxJ8T9l1iw9sMUe0Wyuw; Version=1; Expires=Fri, 24-Sep-2021 17:45:26 GMT; Max-Age=31536000; Path=/; HttpOnly";
		Cookie[] cookies = new Cookie[] { new Cookie("Set-Cookie", cookie) };
		String postResponse = "{\r\n" + "    \"ok\": true,\r\n" + "    \"name\": \"subhi\",\r\n"
				+ "    \"roles\": [\r\n" + "        \"_admin\",\r\n" + "        \"national_admin\",\r\n"
				+ "        \"mm-online\"\r\n" + "    ]\r\n" + "}";
		String getResponse = "{\r\n" + "  \"ok\": true,\r\n" + "  \"userCtx\": {\r\n" + "    \"name\": \"subhi\",\r\n"
				+ "    \"roles\": [\r\n" + "      \"_admin\",\r\n" + "      \"national_admin\",\r\n"
				+ "      \"mm-online\"\r\n" + "    ]\r\n" + "  },\r\n" + "  \"info\": {\r\n"
				+ "    \"authentication_db\": \"_users\",\r\n" + "    \"authentication_handlers\": [\r\n"
				+ "      \"cookie\",\r\n" + "      \"default\"\r\n" + "    ],\r\n"
				+ "    \"authenticated\": \"cookie\"\r\n" + "  }\r\n" + "}";
		HttpHeaders headers = new HttpHeaders();
		HttpHeaders headers1 = new HttpHeaders();
		HttpUtil httpUtil = new HttpUtil();
		AuthenticationDTO request = new AuthenticationDTO();
		request.setPassword("test");
		request.setUser("test");
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setName(request.getUser());
		requestDTO.setPassword(request.getPassword());
		headers.add("Authorization", httpUtil.getBasicAuth(request));
		headers1.addAll("Set-Cookie", Arrays.asList(cookie));
		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO, headers);
		ResponseEntity<String> postResponseEntity = ResponseEntity.status(HttpStatus.OK).headers(headers1).build();
		when(restTemplate.postForEntity(url, entity, String.class))
						.thenReturn(new ResponseEntity<>(postResponse, HttpStatus.OK));
		Mockito.when(authenticationServiceImpl.getLoginSessionDetails(url, entity))
				.thenReturn(new ResponseEntity<>(postResponse, HttpStatus.OK));
		
		when(authenticationServiceImpl.addLoginDetails(request, httpResponse, httpRequest, redirect)).thenReturn(httpResponse);

		assertThat(authenticationServiceImpl.addLoginDetails(request, httpResponse, httpRequest, redirect)).isEqualTo(httpResponse);
//		RequestDTO requestDTO = new RequestDTO();
//		requestDTO.setName(request.getUser());
//		requestDTO.setPassword(request.getPassword());
//		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO, headers);
//
//		ResponseEntity<String> postResponseEntity = ResponseEntity.status(HttpStatus.OK).headers(headers1).build();
//
//		when(restTemplate.postForEntity(url, entity, String.class)).thenReturn(postResponseEntity);
//		when(cookieBase.getCookieSession(postResponseEntity)).thenReturn(cookie);
//		headers.add("Cookie", cookie);
//		//when(authenticationServiceImpl.getSessionDetails(cookie)).thenReturn(getResponse);
//		ResponseEntity<String> getUserctxResponseEntity = new ResponseEntity<>(getResponse, HttpStatus.OK);
//		JsonNode sessionnode;
//		ObjectMapper objectMapper = new ObjectMapper();
//		sessionnode = objectMapper.readTree(getResponse);
//		headers.add("X-Medic-User", sessionnode.get("userCtx").get("name").asText());
//		httpResponse.setContentType("text/html");
//		when(cookieBase.setCookie(getUserctxResponseEntity, httpResponse, httpRequest, redirect)).thenReturn("/");
//		httpResponse.sendRedirect("/");
//	
//		authenticationServiceImpl.addLoginDetails(request, httpResponse, httpRequest, redirect);
//
//
//		assertEquals("text/html", httpResponse.getContentType());

	}

	@Test
	void testAddLoginDetails_throwSessionUnAuthorizedException() {
		String url = authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE;
		String redirect = "";
		HttpHeaders headers = new HttpHeaders();

		HttpUtil httpUtil = new HttpUtil();
		AuthenticationDTO request = new AuthenticationDTO();
		request.setPassword("test");
		request.setUser("test");
		headers.add("Authorization", httpUtil.getBasicAuth(request));
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setName(request.getUser());
		requestDTO.setPassword(request.getPassword());
		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO, headers);
		ResponseEntity<String> expectedResponse = new ResponseEntity<>("Success", headers, HttpStatus.OK);

		when(restTemplate.postForEntity(url, entity, String.class)).thenReturn(expectedResponse);

		Exception exception = assertThrows(SessionUnAuthorisedException.class, () -> {
			authenticationServiceImpl.addLoginDetails(request, httpResponse, httpRequest, redirect);
		});
		assertEquals("Not logged in", exception.getMessage());

	}



	@Test
	void testaddSessionDetailsThrowError() {
		String cookie = "test";
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.GET, entity, String.class))
						.thenThrow(new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR));
		Exception exception = assertThrows(UnauthorizedException.class, () -> {
			authenticationServiceImpl.getSessionDetails(cookie);
		});
		assertEquals("unauthorized", exception.getMessage());
	}

	@Test
	void testDeleteSessionDetails() {
		String cookie = "test";
		String expectedResponse = "Success";
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.DELETE, entity, String.class))
						.thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));
		String actualResponse = authenticationServiceImpl.deleteSessionDetails(cookie);
		assertEquals(expectedResponse, actualResponse);

	}

	@Test
	void testDeleteSessionDetailsThrowError() {
		String cookie = "test";
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		ResponseEntity<String> value = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.DELETE, entity, String.class)).thenReturn(value);
		Exception exception = assertThrows(UnauthorizedException.class, () -> {
			authenticationServiceImpl.deleteSessionDetails(cookie);
		});
		assertEquals("unauthorized", exception.getMessage());

	}

	@Test
	void testDeleteSessionDetails_ThrowErrorForCookie() {
		String cookie = AuthenticationConstants.MISSING;
		HttpEntity<String> entity = httpUtil.getHttpEntity(cookie);
		ResponseEntity<String> value = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
		when(restTemplate.exchange(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				HttpMethod.DELETE, entity, String.class)).thenReturn(value);
		Exception exception = assertThrows(UnauthorizedException.class, () -> {
			authenticationServiceImpl.deleteSessionDetails(cookie);
		});
		assertEquals("unauthorized", exception.getMessage());

	}

	@Test
	void testGetLoginSessionDetails() {

		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setName("Testuser");
		requestDTO.setPassword("Testuser");
		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO);
		ResponseEntity<String> expectedResponse = new ResponseEntity<>("Success", HttpStatus.OK);
		when(restTemplate.postForEntity(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				entity, String.class)).thenReturn(expectedResponse);
		ResponseEntity<String> actualResponse = authenticationServiceImpl.getLoginSessionDetails(
				authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE, entity);
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	void testGetLoginSessionDetails_throwSessionUnAuthorisedException() {
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setName("Testuser");
		requestDTO.setPassword("Testuser");
		HttpEntity<RequestDTO> entity = new HttpEntity<>(requestDTO);
		ResponseEntity<String> expectedResponse = new ResponseEntity<>("Success", HttpStatus.BAD_REQUEST);
		when(restTemplate.postForEntity(authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE,
				entity, String.class)).thenReturn(expectedResponse);
		Exception exception = assertThrows(SessionUnAuthorisedException.class, () -> {
			authenticationServiceImpl.getLoginSessionDetails(
					authenticationServiceImpl.serviceUrl + AuthenticationConstants.SESSION_DATABASE, entity);
		});
		assertEquals("Not logged in", exception.getMessage());
	}

	@Test
	void testLoadSettings() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree("{\"_id\":\"settings\"}");
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		when(authenticationRepository.get(AuthenticationConstants.SETTINGS)).thenReturn(actualObj);
		JsonNode expectedObj = authenticationServiceImpl.loadSettings();
		assertEquals(expectedObj, actualObj);
	}

	@Test
	void testUserInfo() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree("{\"_id\":\"settings\"}");
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		when(authenticationRepository.get(AuthenticationConstants.USER_NAME)).thenReturn(actualObj);
		JsonNode expectedObj = authenticationServiceImpl.userInfo(db, AuthenticationConstants.USER_NAME);
		assertEquals(expectedObj, actualObj);
	}

	@Test
	void testCreateUserSetting() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.readTree("{\"username\":\"subhi\"}");
		JsonNode settings = mapper.readTree("{\"result\":\"subhi\"}");
		when(cookieBase.getSettingUpdates(data.get(AuthenticationConstants.USER_NAME).asText(), data))
				.thenReturn(settings);
		String settingId = authenticationServiceImpl.createId + ":"
				+ data.get(AuthenticationConstants.USER_NAME).asText();
		settings = ((ObjectNode) settings).put(AuthenticationConstants.DOCUMNENT_ID, settingId);
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode actualObj = mapper.readTree("{\"_rev\":\"123\"}");
		when(authenticationRepository.get(settingId)).thenReturn(actualObj);
		JsonNode expectedObj = authenticationServiceImpl.createUserSetting(data, db);
		assertEquals(actualObj.get(AuthenticationConstants.REV_ID), expectedObj.get(AuthenticationConstants.REV_ID));
	}

	@Test
	void testValidateNewUserNameForDb() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode actualObj = mapper.readTree("{\"_rev\":\"123\"}");
		String settingId = authenticationServiceImpl.createId + ":" + "user";
		when(authenticationRepository.get(settingId)).thenReturn(actualObj);
		JsonNode expectedObj = authenticationServiceImpl
				.validateNewUserNameForDb("user", db);
		assertEquals(actualObj.asText(), expectedObj.asText());
	}

	@Test
	void testValidateUserOrSetting() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		authenticationRepository = new AuthenticationRepository(couchDbConnector);
		JsonNode actualObj = mapper.readTree("{\"_rev\":\"123\"}");
		String settingId = authenticationServiceImpl.createId + ":" + "user";
		when(authenticationRepository.get(settingId)).thenReturn(actualObj);
		JsonNode expectedObj = authenticationServiceImpl
				.validateUserOrSetting("user", db);
		assertEquals(actualObj.asText(), expectedObj.asText());
	}
	
	@Test
	void testUpdateUserOrSetting() throws JsonMappingException, JsonProcessingException {
		String db = AuthenticationConstants.MEDIC_DB;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree("{\"_rev\":\"123\"}");
		when(couchDbInstance.createConnector(db, true)).thenReturn(couchDbConnector);
		//authenticationRepository = new AuthenticationRepository(couchDbConnector);
		authenticationServiceImpl.updateUserOrSetting(actualObj,db);
		authenticationRepository.update(actualObj);
		Mockito.verify(authenticationRepository, times(1)).update(actualObj);
	}
}
