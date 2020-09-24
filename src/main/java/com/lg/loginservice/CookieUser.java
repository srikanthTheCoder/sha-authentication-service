package com.lg.loginservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.ws.rs.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lg.constant.AuthenticationConstants;
import com.lg.constant.ErrorMessageConstant;
import com.lg.service.impl.AuthenticationServiceImpl;


/**
 * To add the user details in the database for the admin user
 */

@Component
public class CookieUser {

	@Value("${authenticationservice.userPrefix}")
	String createId;
	@Autowired
	private AuthenticationServiceImpl authenticationServiceImpl;

	private static Map<String, List<String>> roles = new HashMap<>();

	static {
		roles.put(AuthenticationConstants.NATIONAL_MANAGER, Arrays.asList(AuthenticationConstants.KUJUA_USER,
				AuthenticationConstants.DATA_ENTRY, AuthenticationConstants.NATIONAL_ADMIN));
		roles.put(AuthenticationConstants.DISTRICT_MANAGER, Arrays.asList(AuthenticationConstants.KUJUA_USER,
				AuthenticationConstants.DATA_ENTRY, AuthenticationConstants.DISTRICT_ADMIN));
		roles.put(AuthenticationConstants.FACILITY_MANAGER,
				Arrays.asList(AuthenticationConstants.KUJUA_USER, AuthenticationConstants.DATA_ENTRY));
		roles.put(AuthenticationConstants.DATA_ENTRY, Arrays.asList(AuthenticationConstants.DATA_ENTRY));
		roles.put(AuthenticationConstants.ANALYTICS, Arrays.asList(AuthenticationConstants.KUJUA_ANALYTICS));
		roles.put(AuthenticationConstants.GATEWAY, Arrays.asList(AuthenticationConstants.KUJUA_GATEWAY));
	}

	private static String[] metaFields = new String[] {"token_login"};	
	
	private static String[] restrictedUserEditableFields = new String[] {"password","known"};
	
	private static String[] restrictedSettingsEditableFields = new String[] {"fullname","email","phone","language","known"};
	
	private static Object[] firstConcatMetaUserEditableField = Stream.of(metaFields, restrictedUserEditableFields).flatMap(Stream::of).toArray();
	
	private static Object[] allowedRestrictedEditableFields =  Stream.of(firstConcatMetaUserEditableField, restrictedSettingsEditableFields).flatMap(Stream::of).toArray(); 
	


	/**
	 * To validate the username 
	 */
	public void validateNewUserName(String username) {
		Pattern pattern = Pattern.compile(AuthenticationConstants.VALIDATE_USERNAME);
		Matcher matcher = pattern.matcher(username);
		if (!matcher.matches()) {
			throw new NotFoundException(ErrorMessageConstant.INVALID_USER);
		}
		authenticationServiceImpl.validateNewUserNameForDb(username, AuthenticationConstants.USER_DB);
		authenticationServiceImpl.validateNewUserNameForDb(username, AuthenticationConstants.MEDIC_DB);

	}


	/**
	 * To add user details are in the medic/user database for admin user
	 */
	public void createAdmin(JsonNode userctx) {
		validateNewUserName(userctx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.USER).asText());
		authenticationServiceImpl.createUserSetting(userctx, AuthenticationConstants.USER_DATABASE);
		authenticationServiceImpl.createUserSetting(userctx, AuthenticationConstants.MEDIC_DB);
	}


	/**
	 * To get the document id
	 */
	public String getDocId(JsonNode documentId) {
		String docId = null;
		if (documentId.getClass().isInstance(String.class)) {
			docId = documentId.asText();
		}
		if (documentId.getClass().isInstance(Object.class)) {
			docId = documentId.get(AuthenticationConstants.DOC_ID).asText();
		}
		return docId;
	}


	/**
	 * To get user details from the medic/user database 
	 */
	public JsonNode getUserSettings(JsonNode userctx) {
		ObjectNode medicUser;
		JsonNode userResponseJson = authenticationServiceImpl.userInfo(AuthenticationConstants.USER_DB,
				AuthenticationConstants.COUCH_DATABASE_USER
						+ userctx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.NAME).asText());

		JsonNode responseJson = authenticationServiceImpl.userInfo(AuthenticationConstants.MEDIC_DB,
				AuthenticationConstants.COUCH_DATABASE_USER
						+ userctx.get(AuthenticationConstants.USER_CONTEXT).get(AuthenticationConstants.NAME).asText());
		medicUser = (ObjectNode) responseJson;
		if (responseJson.get(AuthenticationConstants.NAME) == null)
			medicUser.put(AuthenticationConstants.NAME,
					userResponseJson.get(AuthenticationConstants.NAME) != null
							? userResponseJson.get(AuthenticationConstants.NAME).asText()
							: "");
		if (responseJson.get(AuthenticationConstants.ROLES).isEmpty())
			medicUser.put(AuthenticationConstants.ROLES,
					userResponseJson.get(AuthenticationConstants.ROLES) != null
							? userResponseJson.get(AuthenticationConstants.ROLES).toString()
							: "");
		if (responseJson.get(AuthenticationConstants.FACILITY_ID) == null)
			medicUser.put(AuthenticationConstants.FACILITY_ID,
					userResponseJson.get(AuthenticationConstants.DOC_ID) != null
							? userResponseJson.get(AuthenticationConstants.DOC_ID).asText()
							: "");
		return medicUser;
	}


	/**
	 * To get role for the user 
	 */
	public String[] getRole(String type) {
		List<String> roleType = new ArrayList<>();

		if (type.isEmpty()) {
			roleType.add("");
		}
		roleType = roles.containsKey(type) ? roles.get(type) : new ArrayList<>();
		if (!roleType.contains(type)) {
			roleType.add(type);
		}
		return  roleType.stream().toArray(String[] ::new);
	}
	

	/**
	 * To restrict illegal modification attempt
	 */
	public boolean illegalDataModificationAttempts(JsonNode data) {
		return Arrays.asList(allowedRestrictedEditableFields).stream().allMatch(e -> data.has(e.toString()));
	}
}
