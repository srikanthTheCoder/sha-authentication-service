package com.lg.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;

@Repository
public class AuthenticationRepository extends CouchDbRepositorySupport<JsonNode> {

	/*
	 * To create the repository for the schema of the database. It will dynamically
	 * create or change the schema from controller
	 */
	public AuthenticationRepository(CouchDbConnector db) {
		super(JsonNode.class, db);
	}
}
