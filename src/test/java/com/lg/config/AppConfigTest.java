package com.lg.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({ MockitoExtension.class, SpringExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
class AppConfigTest {

	public MockMvc mockMvc;

	@InjectMocks
	private AppConfig appConfig;

	@BeforeEach
	void setUp() throws Exception {
		ReflectionTestUtils.setField(appConfig, "url", "https://smarthealth-subhi.lg-apps.com/");
		ReflectionTestUtils.setField(appConfig, "username", "subhi");
		ReflectionTestUtils.setField(appConfig, "password", "C7KrKxFfu3uUy2Kw");
		ReflectionTestUtils.setField(appConfig, "database", "medic");
	}

	@Test
	void shouldReturn_couchdb_connection() throws MalformedURLException {
		HttpClient httpClient = new StdHttpClient.Builder().url(appConfig.url).username(appConfig.username)
				.password(appConfig.password).build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		CouchDbConnector connector = dbInstance.createConnector(appConfig.database, true);
		assertNotNull(connector);
	}
	
	
}
