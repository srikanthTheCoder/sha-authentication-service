package com.lg.model;

import lombok.Data;

/*
 * To Pass the Request from Login
*/
@Data
public class AuthenticationDTO {
	
	private String user;
	private String password;

}
