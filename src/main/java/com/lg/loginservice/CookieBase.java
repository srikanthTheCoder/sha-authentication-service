package com.lg.loginservice;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.ektorp.CouchDbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lg.constant.AuthenticationConstants;
import com.lg.model.UserResponseDTO;
import com.lg.service.impl.AuthenticationServiceImpl;
import com.lg.util.HttpUtil;


/**
 * To set cookie details in header and redirect URL
 */

@Component
public class CookieBase {

	private static final Logger logger = LoggerFactory.getLogger(CookieBase.class);

	@Value("${authenticationservice.url}")
	String url;
	private String cookieDetails;
	CouchDbConnector couchDbConnector;
	@Autowired
	private CookieUser cookieUser;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private HttpUtil httpUtil;
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private AuthenticationServiceImpl authenticationServiceImpl;

	private static String[] restrictedSettingsEditableField = new String[] { "fullname", "email", "phone", "language",
			"known" };

	private static Object[] userEditableFields = Stream
			.of(restrictedSettingsEditableField, (new String[] { "place", "type", "roles" })).flatMap(Stream::of)
			.toArray();

	public String getCookieDetails() {
		return cookieDetails;
	}


	/**
	 * get the cookie from http header
	 */	
	public String getCookieSession(ResponseEntity<String> response) {
		cookieDetails = response.getHeaders().get("Set-Cookie").get(0);
		logger.debug("session from post request {}",cookieDetails);
		return cookieDetails;
	}


	/**
	 * set all the cookies in  http header and redirect the URL
	 */	
	public String setCookie(ResponseEntity<String> response, HttpServletResponse httpResponse,
			HttpServletRequest httpRequest, String redirect) {
		String resCookie = getCookieDetails();
		if (resCookie.isEmpty()) {
			throw new NotFoundException("Cookie not found.");
		}
		setCookieDetails("session", resCookie, httpResponse);
		JsonNode userctx;
		try {
			logger.info("UserCtx cookie");
			setUserCtxCookie(mapper.readTree(response.getBody()), httpResponse);
			userctx = cookieUser.getUserSettings(mapper.readTree(response.getBody()));
			if (userctx == null && mapper.readTree(response.getBody()).get(AuthenticationConstants.USER_CONTEXT)
					.get(AuthenticationConstants.ROLES).has(AuthenticationConstants.ADMIN)) {
				cookieUser.createAdmin(mapper.readTree(response.getBody()));
				userctx = cookieUser.getUserSettings(mapper.readTree(response.getBody()));
			}

			String lang = language(httpRequest.getLocale().getLanguage(),
					userctx != null ? userctx.get(AuthenticationConstants.LANGUAGE).asText() : null,
					get(AuthenticationConstants.LOCALE).toString());
			logger.debug("language {}",lang);
			updateUserlanguageRequirement(
					mapper.readTree(response.getBody()).get(AuthenticationConstants.USER_CONTEXT)
							.get(AuthenticationConstants.NAME).asText(),
					userctx != null ? userctx.get(AuthenticationConstants.LANGUAGE).asText() : "", lang);
			JsonNode localeSetting = get(AuthenticationConstants.LOCALE);
			setCookieDetails(AuthenticationConstants.LOCALE, localeSetting.asText(), httpResponse);
			logger.info("redirecting the URL");
			return getRedirectUrl(mapper.readTree(response.getBody()), redirect);
		} catch (JsonMappingException ex) {
			ex.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return resCookie;
	}


	/**
	 * set the cookie in http header
	 */	
	public void setCookieDetails(String type, String content, HttpServletResponse httpResponse) {
		Cookie cookie = null;
		if (type.equals(AuthenticationConstants.SESSION)) {
			httpResponse.addHeader(AuthenticationConstants.SET_COOKIE, content + AuthenticationConstants.COOKIE_OPTION);
		} else {
			if (type.equals(AuthenticationConstants.LOCALE)) {
				cookie = httpUtil.setCookieParam(AuthenticationConstants.LOCALE, content);
			} else if (type.equals(AuthenticationConstants.USER_CONTEXT)) {
				cookie = httpUtil.setCookieParam(AuthenticationConstants.USER_CONTEXT, content);
			}
			httpResponse.addCookie(cookie);
		}
	}


	/**
	 * set the userCtx cookie in the http header
	 */	
	public void setUserCtxCookie(JsonNode userCtx, HttpServletResponse httpResponse) {
		String home = getHomeUrl(userCtx);
		JsonNode context;
		if (home.equals("")) {
			context = userCtx;
		} else {
			userCtx = ((ObjectNode) userCtx).put(AuthenticationConstants.PATH, home);
			context = userCtx;
		}
		setCookieDetails(AuthenticationConstants.USER_CONTEXT,
				context.get(AuthenticationConstants.USER_CONTEXT).toString(), httpResponse);
	}


	/**
	 * get the home URL based on online or offline user
	 */	
	public String getHomeUrl(JsonNode userCtx) {
		if (isOnlineOnly(userCtx) && hasAllPermissions(userCtx, "can_configure")) {
			return AuthenticationConstants.ADMIN_PATH;
		} else {
			return "";
		}
	}


	/**
	 * get the redirect URL
	 */	
	public String getRedirectUrl(JsonNode userctx, String requested) {
		String root = getHomeUrl(userctx);
		logger.debug("requested URL {}",requested);
		if (requested.isEmpty()) {
			return root;
		}
		try {
			URI req = new URI(requested);
			String path = req.getPath();
			String hash = req.getFragment();
			return path + (hash != null ? hash : "");
		} catch (Exception e) {
			return root;
		}
	}


	/**
	 * check whether the user is online user
	 */	
	public boolean isOnlineOnly(JsonNode userCtx) {
		if (userCtx.get(AuthenticationConstants.USER_CONTEXT) != null) {
			return ((checkUserRolesExist(
					userCtx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.ROLES),
					(AuthenticationConstants.ADMIN)))
					|| (checkUserRolesExist(
							userCtx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.ROLES),
							(AuthenticationConstants.NATIONAL_ADMIN)))
					|| (!isOffline(userCtx)));
		}
		return false;
	}


	/**
	 * check whether the user is offline user
	 */	
	public boolean isOffline(JsonNode userCtx) {
		JsonNode configured = get(AuthenticationConstants.ROLES);
		List<String> configuredList = getKeyValues(configured);
		JsonNode roles = userCtx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.ROLES);
		List configuredRoles = configuredList.stream().filter(e -> checkUserRolesExist(roles, e))
				.collect(Collectors.toList());
		return (!checkUserRolesExist(roles, (AuthenticationConstants.ADMIN))
				&& !configuredRoles.contains(AuthenticationConstants.ADMIN)
				&& checkOffline(configuredRoles, configured));

	}


	/**
	 * Check whether the user has the mentioned role like admin, national admin
	 */	
	public boolean checkUserRolesExist(JsonNode value, String role) {
		ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
		});
		List<String> rolesList = null;
		try {
			rolesList = reader.readValue(value);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return rolesList != null ? rolesList.contains(role) : Boolean.FALSE;
	}


	/**
	 * Check whether the role is offline user role
	 */	
	public boolean checkOffline(List configuredRoles, JsonNode configured) {
		boolean offline = false;
		for (Object role : configuredRoles) {
			offline = configured.get(role.toString()).has(AuthenticationConstants.OFFLINE);
			if (offline) {
				break;
			}
		}
		return offline;
	}


	/**
	 * Check whether the user has all the permission
	 */	
	public boolean hasAllPermissions(JsonNode userCtx, String permission) {
		if (checkUserRolesExist(userCtx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.ROLES),
				(AuthenticationConstants.ADMIN)))
			return true;
		else
			return hasPermission(userCtx, permission);
	}


	/**
	 * Check whether the user has mentioned permission
	 */	
	public boolean hasPermission(JsonNode userctx, String permission) {
		JsonNode roles = get("permissions");
		JsonNode permissions = (roles.get(permission));
		ArrayList<String> rolesList = mapper.convertValue(
				(userctx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.ROLES)),
				ArrayList.class);
		List hasPermissions = rolesList.stream().filter(e -> checkUserRolesExist(permissions, e))
				.collect(Collectors.toList());
		return !hasPermissions.isEmpty();
	}


	/**
	 * get the field name of the data
	 */	
	public List getKeyValues(JsonNode value) {
		return StreamSupport.stream(value.spliterator(), false).map(e -> e.fields().next().getKey())
				.collect(Collectors.toList());
	}


	/**
	 * get the update user data to insert in database
	 */	
	public JsonNode getSettingUpdates(String username, JsonNode data) {
		Map setting = new HashMap<>();
		String[] role = null;
		setting.put(AuthenticationConstants.NAME, username);
		setting.put(AuthenticationConstants.TYPE, AuthenticationConstants.USER_SETTINGS);
		if (data.get(AuthenticationConstants.TYPE) != null) {
			role = cookieUser.getRole(data.get(AuthenticationConstants.TYPE).toString());
			setting.put(AuthenticationConstants.ROLES, role);
		}
		if (setting.get(AuthenticationConstants.ROLES) != null) {
			List roleSetting = Arrays.asList(role);
			if (isOffline(data)) {
				if (roleSetting.contains(AuthenticationConstants.ONLINE_ROLE)) {
					roleSetting.remove(AuthenticationConstants.ONLINE_ROLE);
				}
			} else {
				roleSetting.add(AuthenticationConstants.ONLINE_ROLE);
			}
			setting.put(AuthenticationConstants.ROLES, roleSetting);
		}
		if (data.get(AuthenticationConstants.PLACE) != null) {
			String documentId = cookieUser.getDocId(data.get(AuthenticationConstants.PLACE));
			setting.put(AuthenticationConstants.FACILITY_ID, documentId);
		}

		if (data.get(AuthenticationConstants.CONTACT) != null) {
			String documentId = cookieUser.getDocId(data.get(AuthenticationConstants.CONTACT));
			setting.put(AuthenticationConstants.CONTACT_ID, documentId);
		}

		if (data.get(AuthenticationConstants.LANGUAGE) != null
				&& data.get(AuthenticationConstants.LANGUAGE).get(AuthenticationConstants.CODE) != null) {
			setting.put(AuthenticationConstants.LANGUAGE,
					data.get(AuthenticationConstants.LANGUAGE).get(AuthenticationConstants.CODE));
		}
		return objectMapper.valueToTree(setting);

	}


	/**
	 * get the update user data
	 */	
	public JsonNode getUserUpdate(String username, JsonNode data) {
		ArrayList<String> ignore = new ArrayList<>();
		ignore.add("type");
		ignore.add("place");
		List editableFields = Arrays.asList(userEditableFields);

		Map user = new HashMap<>();
		String[] role = null;
		user.put(AuthenticationConstants.NAME, username);
		user.put(AuthenticationConstants.TYPE, AuthenticationConstants.USER);

		for (Object key : editableFields) {
			if (data.has(key.toString()) && !ignore.contains(key)) {
				user.put(key, data.get(key.toString()));
			}
		}

		if (data.get(AuthenticationConstants.TYPE) != null) {
			role = cookieUser.getRole(data.get(AuthenticationConstants.TYPE).toString());
			user.put(AuthenticationConstants.ROLES, role);
		}

		if (role != null && !isOffline(data)) {
			user.put(AuthenticationConstants.ROLES, role);
		}

		if (data.get(AuthenticationConstants.PLACE) != null) {
			String facilityId = cookieUser.getDocId(data.get(AuthenticationConstants.PLACE));
			user.put(AuthenticationConstants.FACILITY_ID, facilityId);
		}

		return objectMapper.valueToTree(user);
	}


	/**
	 * get the language for the user
	 */	
	public String language(String requestedLanguage, String lang, String locale) {
		if (requestedLanguage != null) {
			return requestedLanguage;
		} else if (lang != null) {
			return lang;
		}
		return locale != null ? locale : AuthenticationConstants.LOCALE_EN;
	}


	/**
	 * get the value from settings documents based on the key passed in it
	 */	
	public JsonNode get(String key) {
		JsonNode keyValue = UserResponseDTO.getResponseSetting().get(AuthenticationConstants.SETTINGS).get(key) != null
				? UserResponseDTO.getResponseSetting().get(AuthenticationConstants.SETTINGS).get(key)
				: UserResponseDTO.getResponseSetting();
		return keyValue;
	}


	/**
	 * update the language, if the user language selected language is differ from current language
	 */	
	public void updateUserlanguageRequirement(String user, String current, String selected) {
		if (current.equals(selected)) {
			return;
		}
		ObjectNode objNode = objectMapper.createObjectNode();
		updateUser(user, objNode.put(AuthenticationConstants.LANGUAGE, selected));
	}
	
	/**
	 * update the data for the user in the medic and user database.
	 */
	public void updateUser(String user, JsonNode data) {
		JsonNode users = getUpdatedUserOrSettingDoc(user, data, AuthenticationConstants.USER_DB);
		JsonNode settings = getUpdatedUserOrSettingDoc(user, data, AuthenticationConstants.MEDIC_DB);
		if (!data.has(AuthenticationConstants.PLACE)) {
			if (settings.has(AuthenticationConstants.ROLES) && isOffline(settings.get(AuthenticationConstants.ROLES))) {
				throw new BadRequestException("Place field is required for offline users");
			}
			((ObjectNode) users).put(AuthenticationConstants.FACILITY_ID, "null");
			((ObjectNode) settings).put(AuthenticationConstants.FACILITY_ID, "null");
		}
		if (!data.has(AuthenticationConstants.CONTACT)) {
			if (settings.has(AuthenticationConstants.ROLES) && isOffline(settings.get(AuthenticationConstants.ROLES))) {
				throw new BadRequestException("Place field is required for offline users");
			}
			((ObjectNode) settings).put(AuthenticationConstants.CONTACT_ID, "null");
		}

		authenticationServiceImpl.updateUserOrSetting(users, AuthenticationConstants.USER_DB);
		authenticationServiceImpl.updateUserOrSetting(settings, AuthenticationConstants.MEDIC_DB);
	}


	/**
	 * update the user and setting document for the user to
	 * insert in the user and medic database respectively.
	 */
	public JsonNode getUpdatedUserOrSettingDoc(String username, JsonNode data, String db) {
		JsonNode userOrSettings = authenticationServiceImpl.validateUserOrSetting(username, db);
		((ObjectNode) userOrSettings).put(AuthenticationConstants.LANGUAGE,
				data.get(AuthenticationConstants.LANGUAGE).asText());
		return userOrSettings;
	}

}
