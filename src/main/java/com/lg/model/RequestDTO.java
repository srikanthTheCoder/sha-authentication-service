package com.lg.model;

import lombok.Data;

/*
 * To pass the Request to Couch DB
 */
@Data
public class RequestDTO {
	
	private String name;
	private String password;

}
