package com.lg.model;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDTO {
	private static JsonNode responseSetting;

	public static JsonNode getResponseSetting() {
		return responseSetting;
	}

	public static void setResponseSetting(JsonNode responseSetting) {
		UserResponseDTO.responseSetting = responseSetting;
	}
}
