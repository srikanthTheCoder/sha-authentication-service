package com.lg.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthenticationRepositoryTest {

	public MockMvc mockMvc;
	
	@InjectMocks
	private AuthenticationRepository authenticationRepository;
	
	@Mock
	CouchDbConnector dbconnector;
	
	@Mock
	CouchDbInstance dbInstance;
	
	@Mock
	HttpClient httpClient;
	
	@BeforeEach
	void setUp() throws Exception {
		httpClient = new StdHttpClient.Builder().url("https://smarthealth-subhi.lg-apps.com/").username("subhi").password("C7KrKxFfu3uUy2Kw").build();
		 dbInstance = new StdCouchDbInstance(httpClient);
		dbconnector = dbInstance.createConnector("medic", true);
		authenticationRepository = new AuthenticationRepository(dbconnector);
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationRepository).build();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void test() {
		verifyNoMoreInteractions(dbInstance);
		verifyNoMoreInteractions(dbconnector);
	}

}
